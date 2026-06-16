-- ============================================================
-- V2: Inventory + Historical Product
-- ============================================================


CREATE TABLE IF NOT EXISTS historical_products (
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

CREATE TABLE IF NOT EXISTS historical_product_images (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    historical_product_id   BIGINT NOT NULL,
    image_url               VARCHAR(500) NOT NULL,
    position                INT NOT NULL,
    INDEX idx_hpi_hp (historical_product_id)
);

CREATE TABLE IF NOT EXISTS historical_product_variants (
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

CREATE TABLE IF NOT EXISTS inventory_items (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_variant_id  BIGINT NOT NULL UNIQUE,
    sku                 VARCHAR(100),
    cached_stock        INT NOT NULL DEFAULT 0,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stock_movements (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_item_id   BIGINT NOT NULL,
    movement_type       ENUM('RESTOCK','SALE','CANCEL','ADJUSTMENT') NOT NULL,
    quantity            INT NOT NULL,
    reference_order_id  BIGINT NULL,
    note                VARCHAR(255),
    created_at          DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_sm_inventory (inventory_item_id)
);
