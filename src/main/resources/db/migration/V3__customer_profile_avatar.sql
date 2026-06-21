-- ============================================================
-- V3: Avatar cho customer profile (Cloudinary)
-- ============================================================

ALTER TABLE customer_profiles
    ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500) NULL;
