ALTER TABLE orders ADD COLUMN delivered_at DATETIME NULL;

-- Backfill đơn hàng đã ở trạng thái DELIVERED trước khi có cột này, dùng updated_at làm mốc gần đúng
UPDATE orders SET delivered_at = updated_at WHERE status = 'DELIVERED' AND delivered_at IS NULL;
