/*
Luồng chính xuất kho
    Tạo yêu cầu xuất kho (Stock Out Request)
    Duyệt yêu cầu (Approve Request)
    Thực hiện xuất kho (Process Stock Out)
    Cập nhật tồn kho (Update Inventory)
    Ghi log tồn kho (Log Inventory Change)
    Xuất dữ liệu kế toán (Export to Accounting)

    1.Tao yeu cau xuat kho (Store Procedure)
        insert stock_request
        insert stock_request_details
    2. Duyet yeu cau (Manually)
        manager -> update stock_request 
    3. Cap nhat ton kho (Trigger)


*/
    
DELIMITER //

CREATE PROCEDURE create_stock_out_request(
    IN p_employee_id BIGINT,
    IN p_product_details JSON, -- Format: [{"product_id": 1, "quantity": 5}, {...}]
    OUT p_request_id BIGINT,
    OUT p_result BOOLEAN,
    OUT p_message VARCHAR(255)
)
proc_label: BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_product_count INT;
    DECLARE v_product_id BIGINT;
    DECLARE v_quantity INT;
    DECLARE v_selling_price DECIMAL(10, 2);
    DECLARE v_product_exists BOOLEAN DEFAULT FALSE;
    
    -- Khởi tạo biến output
    SET p_result = FALSE;
    SET p_message = '';
    
    -- Bắt đầu transaction
    START TRANSACTION;
    
    -- Tạo yêu cầu xuất kho
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
        SET v_product_id = JSON_UNQUOTE(JSON_EXTRACT(p_product_details, CONCAT('$[', i, '].product_id')));
        SET v_quantity = JSON_EXTRACT(p_product_details, CONCAT('$[', i, '].quantity'));
        
        SELECT COUNT(*) > 0 INTO v_product_exists
        FROM products
        WHERE product_id = v_product_id;
        
        IF v_product_exists = FALSE THEN
            SET p_message = CONCAT('Sản phẩm ID ', v_product_id, ' không tồn tại');
            ROLLBACK;
            LEAVE proc_label;
        END IF;

        SELECT selling_price INTO v_selling_price
        FROM products
        WHERE product_id = v_product_id;
        
        INSERT INTO stock_request_details (
            request_id,
            product_id,
            quantity,
            unit_price
        ) VALUES (
            p_request_id,
            v_product_id,
            v_quantity,
            v_selling_price
        );
        
        SET i = i + 1;
    END WHILE;
    
    COMMIT;
    SET p_result = TRUE;
    SET p_message = CONCAT('Tạo yêu cầu xuất kho #', p_request_id, ' thành công');

END //

DELIMITER ;
