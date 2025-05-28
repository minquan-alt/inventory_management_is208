USE IVM;

DELIMITER //

CREATE TRIGGER tg_after_stock_request_in_approved
AFTER UPDATE ON stock_requests
FOR EACH ROW
BEGIN
	-- chi xu ly khi trang thai tu PENDING sang APPROVED va la nhap kho
	IF OLD.status = 'PENDING' AND NEW.status = 'APPROVED' AND NEW.request_type = 'IN' THEN 
		
        -- Neu san pham co trong kho
        UPDATE inventory i
        JOIN stock_request_details srd ON i.product_id = srd.product_id
		SET i.quantity = i.quantity + srd.quantity
        WHERE srd.request_id = NEW.request_id;
        
        -- Neu san pham khong co trong kho
        INSERT INTO inventory (product_id, quantity, unit_price)
        SELECT srd.product_id, srd.quantity, srd.unit_price
        FROM stock_request_details srd
        LEFT JOIN inventory i ON i.product_id = srd.product_id
        WHERE srd.request_id = NEW.request_id AND i.product_id IS NULL;
		
        -- ghi log ton kho
        INSERT INTO iventory_log (product_id, change_type, quantity_change, reference_id, reference_type, created_by)
        SELECT srd.product_id, 'IN', srd.quantity, NEW.request_id, 'PO', NEW.approved_by
        FROM stock_request_details srd 
		WHERE srd.request_id = NEW.request_id;
        
        -- Tao ban ghi ke toan
        INSERT INTO accounting_exports (exprot_type, reference_id, status)
        VALUES ('PO', NEW.request_id, 'PENDING');
        
	END IF;
END // 
DELIMITER ;
        
        
        
        
        
        