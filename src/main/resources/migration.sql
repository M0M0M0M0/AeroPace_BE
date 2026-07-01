-- ============================================================
-- Migration: Historical Product + Inventory
-- ============================================================

-- 1. historical_products
CREATE TABLE historical_products (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id      BIGINT NOT NULL,
    name            VARCHAR(200),
    description     LONGTEXT,
    brand           VARCHAR(200),
    option1_name    VARCHAR(50),
    option2_name    VARCHAR(50),
    option3_name    VARCHAR(50),
    valid_from      DATETIME(3) NOT NULL,
    valid_to        DATETIME(3) NULL,
    INDEX idx_hp_product_valid (product_id, valid_from, valid_to)
);

-- 2. historical_product_images
CREATE TABLE historical_product_images (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    historical_product_id   BIGINT NOT NULL,
    image_url               VARCHAR(500) NOT NULL,
    position                INT NOT NULL,
    INDEX idx_hpi_hp (historical_product_id)
);

-- 3. historical_product_variants
CREATE TABLE historical_product_variants (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    historical_product_id   BIGINT NOT NULL,
    original_variant_id     BIGINT NULL,
    sku                     VARCHAR(100),
    price                   DECIMAL(19,2) NOT NULL,
    compare_price           DECIMAL(19,2),
    option1_value           VARCHAR(50),
    option2_value           VARCHAR(50),
    option3_value           VARCHAR(50),
    INDEX idx_hpv_hp (historical_product_id)
);

-- 4. inventory_items
CREATE TABLE inventory_items (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_variant_id  BIGINT NOT NULL UNIQUE,
    sku                 VARCHAR(100),
    cached_stock        INT NOT NULL DEFAULT 0,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 5. stock_movements
CREATE TABLE stock_movements (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_item_id   BIGINT NOT NULL,
    movement_type       ENUM('RESTOCK','SALE','CANCEL','ADJUSTMENT') NOT NULL,
    quantity            INT NOT NULL,
    reference_order_id  BIGINT NULL,
    note                VARCHAR(255),
    created_at          DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_sm_inventory (inventory_item_id)
);


INSERT INTO inventory_items (product_variant_id, sku, cached_stock, created_at)
SELECT pv.id, pv.sku, COALESCE(pv.stock, 0), NOW()
FROM product_variants pv
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items ii WHERE ii.product_variant_id = pv.id
);

INSERT INTO stock_movements (inventory_item_id, movement_type, quantity, note, created_at)
SELECT ii.id, 'RESTOCK', ii.cached_stock, 'Initial stock migration', NOW()
FROM inventory_items ii
WHERE ii.cached_stock > 0;

-- 6. Remove stockcolumn
ALTER TABLE product_variants DROP COLUMN stock;
