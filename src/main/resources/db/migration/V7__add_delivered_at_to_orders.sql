ALTER TABLE orders ADD COLUMN delivered_at DATETIME NULL;

UPDATE orders SET delivered_at = updated_at WHERE status = 'DELIVERED' AND delivered_at IS NULL;
