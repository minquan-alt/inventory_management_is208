
USE IVM;


CREATE TABLE employees (
		employee_id BIGINT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        username VARCHAR(50) UNIQUE NOT NULL,
        password_hash VARCHAR(255) NOT NULL,
        role ENUM( 'ROLE_PRODUCT_MANAGEMENT',
				   'ROLE_HUMAN_MANAGEMENT',
				   'ROLE_RECEIPT',
				   'ROLE_ISSUE'),
		status BOOLEAN DEFAULT TRUE
);

CREATE TABLE products (
	product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    unit VARCHAR(50),
    cost_price DECIMAL(10, 2) DEFAULT 0,
    selling_price DECIMAL(10, 2) DEFAULT 0
);

CREATE TABLE stock_requests (
    request_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_type ENUM('IN', 'OUT') NOT NULL,
    employee_id BIGINT,
    status ENUM('PENDING', 'APPROVED', 'DECLINED') DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    approved_by BIGINT,
    approved_at DATETIME,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    FOREIGN KEY (approved_by) REFERENCES employees(employee_id)
);

CREATE TABLE stock_request_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT,
    product_id BIGINT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2),
    FOREIGN KEY (request_id) REFERENCES stock_requests(request_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE inventory (
    inventory_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,

    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE ON UPDATE CASCADE
);



CREATE TABLE inventory_check (
    check_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    note TEXT,
    FOREIGN KEY (created_by) REFERENCES employees(employee_id)
);

CREATE TABLE inventory_check_details (
    check_id BIGINT,
    product_id BIGINT,
    actual_quantity INT NOT NULL,
    system_quantity INT NOT NULL,
    adjustment INT AS (actual_quantity - system_quantity),
    note TEXT,
    PRIMARY KEY(check_id, product_id),
    FOREIGN KEY (check_id) REFERENCES inventory_check(check_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE inventory_logs (
  log_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id    BIGINT NOT NULL,
  change_type   ENUM('IN','OUT','ADJUST') NOT NULL,
  quantity_change    INT NOT NULL,
  reference_id  BIGINT,                 -- so_id hoặc po_id hoặc count_id
  reference_type ENUM('PO','SO','COUNT') NOT NULL,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by  BIGINT NOT NULL,
  FOREIGN KEY (product_id) REFERENCES products(product_id),
  FOREIGN KEY (created_by) REFERENCES employees(employee_id)
);

CREATE TABLE accounting_exports (
  export_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
  export_type   ENUM('PO','SO','REPORT') NOT NULL,
  reference_id  BIGINT NOT NULL,
  exported_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status        ENUM('PENDING','DONE','ERROR') NOT NULL DEFAULT 'PENDING'
);


-- check inventory
ALTER TABLE inventory
ADD CONSTRAINT chk_quantity_non_negative 
CHECK (quantity >= 0);

DELIMITER //
CREATE TRIGGER check_quantity_before_update
BEFORE UPDATE ON inventory
FOR EACH ROW
BEGIN
    IF NEW.quantity < 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Số lượng tồn kho không được âm';
    END IF;
END//
DELIMITER ;

COMMIT;


/*
Luồng chính xuất kho
    Tạo yêu cầu xuất kho (Stock Out Request)
    Duyệt yêu cầu (Approve Request)
    Thực hiện xuất kho (Process Stock Out)
    Cập nhật tồn kho (Update Inventory)
    Ghi log tồn kho (Log Inventory Change)
    Xuất dữ liệu kế toán (Export to Accounting)
*/
DELIMITER //
CREATE PROCEDURE create_stock_out_request(
    IN p_employee_id BIGINT,
    IN p_product_details JSON, -- Format: [{"product_id": 1, "quantity": 5}, {...}]
    OUT p_request_id BIGINT,
    OUT p_result BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_product_count INT;
    DECLARE v_product_id BIGINT;
    DECLARE v_quantity INT;
    DECLARE v_product_exists BOOLEAN;

    -- bien output
    SET p_result = FALSE;
    SET p_message = '';

    START TRANSACTION;

    INSERT INTO stock_requests (
        request_type,
        employee_id,
        status
    ) VALUES (
        'OUT',
        p_employee_id,
        'PENDING'
    );

    SET p_request_id = LAST_INSERT_ID();

    SET v_product_count = JSON_LENGTH(p_product_details);
    WHILE i < v_product_count DO
        SET v_product_id = JSON_EXTRACT(p_product_details, CONCAT('$[', i, '].product_id'));
        SET v_quantity = JSON_EXTRACT(p_product_details, CONCAT('$[', i, '].quantity'));

        SELECT COUNT(*) > 0 INTO v_product_exists
        FROM products 
        WHERE product_id = v_product_id

        IF v_product_exists = FALSE THEN
            SET p_message = CONCAT('Sản phẩm ID ', v_product_id, ' không tồn tại');
            ROLLBACK;
            LEAVE proc_label;
        END IF;

        INSERT INTO stock_request_details (
            request_id,
            product_id,
            quantity
        ) VALUES (
            p_request_id,
            v_product_id,
            v_quantity
        );

        SET i = i + 1;
    END WHILE;

    COMMIT;
    SET p_result = TRUE;
    SET p_message = CONCAT('Tạo yêu cầu xuất kho #', p_request_id, ' thành công');

    proc_label: BEGIN END;
END //
DELIMITTER;