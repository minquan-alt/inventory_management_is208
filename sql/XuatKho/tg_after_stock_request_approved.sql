
DELIMITER //
CREATE TRIGGER tg_after_stock_request_approved
AFTER UPDATE ON stock_requests
FOR EACH ROW
BEGIN
    IF OLD.status = 'PENDING' AND NEW.status = 'APPROVED' AND NEW.request_type = 'OUT' THEN
	
        -- cap nhat ton kho
        UPDATE inventory i
        INNER JOIN stock_request_details srd ON i.product_id = srd.product_id
        SET i.quantity = i.quantity - srd.quantity
        WHERE srd.request_id = NEW.request_id;
        
        -- ghi log
        INSERT INTO inventory_logs (product_id, change_type, quantity_change, reference_id, reference_type, created_by)
        SELECT 
            srd.product_id, 
            'OUT', 
            -srd.quantity, 
            NEW.request_id, 
            'SO', 
            NEW.approved_by
        FROM stock_request_details srd
        WHERE srd.request_id = NEW.request_id;
        
        -- tao record cho ke toan
        INSERT INTO accounting_exports (export_type, reference_id, status)
        VALUES ('SO', NEW.request_id, 'PENDING');
        
    END IF;
END //
DELIMITER ;