USE IVM;
DELIMITER //

CREATE TRIGGER tg_after_stock_request_in_approved
    AFTER UPDATE ON stock_requests
    FOR EACH ROW
BEGIN
    -- Chỉ xử lý khi duyệt yêu cầu nhập kho
    IF OLD.status = 'PENDING' AND NEW.status = 'APPROVED' AND NEW.request_type = 'IN' THEN

        -- Cập nhật sản phẩm đã có trong kho
    UPDATE inventory i
        JOIN stock_request_details srd ON i.product_id = srd.product_id
        SET i.quantity = i.quantity + srd.quantity
    WHERE srd.request_id = NEW.request_id;

    -- Thêm mới sản phẩm chưa có trong kho
    INSERT INTO inventory (product_id, quantity)
    SELECT srd.product_id, srd.quantity
    FROM stock_request_details srd
             LEFT JOIN inventory i ON i.product_id = srd.product_id
    WHERE srd.request_id = NEW.request_id AND i.product_id IS NULL;

    -- Ghi log tồn kho
    INSERT INTO inventory_logs (product_id, change_type, quantity_change, reference_id, reference_type, created_by)
    SELECT
        srd.product_id,
        'IN',
        -srd.quantity,
        NEW.request_id,
        'PO',
        NEW.approved_by
    FROM stock_request_details srd
    WHERE srd.request_id = NEW.request_id;

    -- Tạo bút toán kế toán
    INSERT INTO accounting_exports (export_type, reference_id, status)
    VALUES ('PO', NEW.request_id, 'PENDING');
END IF;
END;
//
DELIMITER ;
        
        
        
        
        
        