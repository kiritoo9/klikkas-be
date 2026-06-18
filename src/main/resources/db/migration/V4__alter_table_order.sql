ALTER TABLE orders
ADD COLUMN order_type VARCHAR(50) default 'kas_masuk';

ALTER TABLE orders
ADD CONSTRAINT chk_orders_order_type
CHECK (order_type IN ('kas_masuk', 'kas_keluar'));