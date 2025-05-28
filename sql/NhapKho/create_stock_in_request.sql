USE IVM;

DELIMITER //

CREATE PROCEDURE create_stock_in_request(
	IN p_employee_id BIGINT,
    IN p_product_details JSON,
    OUT p_request_id BIGINT,
    OUT p_result BOOLEAN,
    OUT p_message VARCHAR(255)
)

proc_lable: BEGIN
	DECLARE i INT DEFAULT 0;
    DECLARE v_product_count INT;
    DECLARE v_product_id BIGINT;
    DECLARE v_quantity INT;
    DECLARE v_unit_price DECIMAL(10, 2);
    DECLARE v_product_exists BOOLEAN DEFAULT FALSE;
    
    -- Khoi tao output mac dinh
	SET p_result = FALSE;
    SET p_message = '';
    
    -- Tao yeu cau nhap kho
    START TRANSACTION;
    
    INSERT INTO stock_requests (request_type, employee_id , status)
    VALUES ( 'IN', p_employee_id, 'PENDING');
    
    SET p_request_id = LAST_INSERT_ID();
    SET v_product_count = JSON_LENGTH(p_product_details);
    
    WHILE i < v_product_count DO
		SET v_product_id = JSON_UNQUOTE(JSON_EXTRACT(p_product_details, CONCAT('$[', i, '].product_id')));
		SET v_quantity = JSON_EXTRACT(p_product_details, CONCAT('$[', i, '].quantity'));
        SET v_unit_price = JSON_EXTRACT(p_product_details, CONCAT('$[', i, '].unit_price'));
        
        -- Kiem tra san pham co ton tai
        SELECT COUNT(*) > 0 INTO v_product_exists
        FROM products
        WHERE product_id = v_product_id;
        
        IF v_product_exists = FALSE THEN
			SET p_message = CONCAT('San pham ID ', v_product_id, ' khong ton tai');
            ROLLBACK;
            LEAVE proc_label;
		END IF;
        
        -- Them vao std
        INSERT INTO stock_request_details (request_id, product_id, quantity, unit_price)
        VALUES (p_request_id, v_product_id, v_quantity, v_unit_price);
        
        SET i = i + 1;
	END WHILE;
    
    COMMIT;
    SET p_result = TRUE;
    SET p_message = CONCAT('Tao yeu cau nhap kho #', p_request_id, ' thanh cong');
	
END //
DELIMITER ;
        
        