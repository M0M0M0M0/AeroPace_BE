-- =====================================================
-- 1. TẠO 12 BIG CATEGORY (INSERT IGNORE — safe nếu đã chạy thủ công)
-- =====================================================
INSERT IGNORE INTO categories (id, name, description) VALUES
(192, 'Apparel',               NULL),
(193, 'Footwear',              NULL),
(194, 'Accessories',           NULL),
(195, 'Hydration',             NULL),
(196, 'Recovery',              NULL),
(197, 'Nutrition',             NULL),
(198, 'Electronics',           NULL),
(199, 'Trail Equipment',       NULL),
(200, 'Pickleball Paddles',    NULL),
(201, 'Pickleball Balls',      NULL),
(202, 'Pickleball Accessories',NULL),
(203, 'Pickleball Bags',       NULL);

-- =====================================================
-- 2. GÁN APPAREL (192)
-- =====================================================
INSERT IGNORE INTO product_category (product_id, category_id)
SELECT DISTINCT pc.product_id, 192
FROM product_category pc
JOIN categories c ON c.id = pc.category_id
WHERE c.id IN (
    15,16,25,43,68,70,82,94,95,109,114,123,124,125,126,
    130,131,132,134,137,138,150,157,158,161,164,172,186
);

-- =====================================================
-- 3. GÁN FOOTWEAR (193)
-- =====================================================
INSERT IGNORE INTO product_category (product_id, category_id)
SELECT DISTINCT pc.product_id, 193
FROM product_category pc
JOIN categories c ON c.id = pc.category_id
WHERE c.id IN (
    9,14,27,28,71,72,80,116,129,133,141,142,146
);

-- =====================================================
-- 4. GÁN ACCESSORIES (194)
-- =====================================================
INSERT IGNORE INTO product_category (product_id, category_id)
SELECT DISTINCT pc.product_id, 194
FROM product_category pc
JOIN categories c ON c.id = pc.category_id
WHERE c.id IN (
    1,2,3,5,11,12,23,24,29,30,31,32,34,35,36,37,38,
    48,51,52,54,57,75,77,78,86,97,98,103,112,113,
    115,117,139,140,144,147,159,160,162,163,165,
    166,169,176,177
);

-- =====================================================
-- 5. GÁN HYDRATION (195)
-- =====================================================
INSERT IGNORE INTO product_category (product_id, category_id)
SELECT DISTINCT pc.product_id, 195
FROM product_category pc
JOIN categories c ON c.id = pc.category_id
WHERE c.id IN (
    4,46,53,58,59,85,154,170,181,185
);

-- =====================================================
-- 6. GÁN RECOVERY (196)
-- =====================================================
INSERT IGNORE INTO product_category (product_id, category_id)
SELECT DISTINCT pc.product_id, 196
FROM product_category pc
JOIN categories c ON c.id = pc.category_id
WHERE c.id IN (
    19,20,56,84,118,143,148,156,168,171,173,179,180,182
);

-- =====================================================
-- 7. GÁN NUTRITION (197)
-- =====================================================
INSERT IGNORE INTO product_category (product_id, category_id)
SELECT DISTINCT pc.product_id, 197
FROM product_category pc
JOIN categories c ON c.id = pc.category_id
WHERE c.id IN (
    7,17,18,42,44,45,47,67,69,73,74,79,88,89,96,99,
    100,104,107,119,120,122,127,135,136,151,155,174,
    175,178,183,184,187
);

-- =====================================================
-- 8. GÁN ELECTRONICS (198)
-- =====================================================
INSERT IGNORE INTO product_category (product_id, category_id)
SELECT DISTINCT pc.product_id, 198
FROM product_category pc
JOIN categories c ON c.id = pc.category_id
WHERE c.id IN (
    22,40,41,49,55,60,61,62,64,65,66,81,83,101,105,
    106,167,188
);

-- =====================================================
-- 9. GÁN TRAIL EQUIPMENT (199)
-- =====================================================
INSERT IGNORE INTO product_category (product_id, category_id)
SELECT DISTINCT pc.product_id, 199
FROM product_category pc
JOIN categories c ON c.id = pc.category_id
WHERE c.id IN (
    6,8,21,76,108,110,111,145,149,152,153,189
);

-- =====================================================
-- 10. GÁN PICKLEBALL PADDLES (200)
-- =====================================================
INSERT IGNORE INTO product_category (product_id, category_id)
SELECT DISTINCT pc.product_id, 200
FROM product_category pc
JOIN categories c ON c.id = pc.category_id
WHERE c.id IN (93);

-- =====================================================
-- 11. GÁN PICKLEBALL BALLS (201)
-- =====================================================
INSERT IGNORE INTO product_category (product_id, category_id)
SELECT DISTINCT pc.product_id, 201
FROM product_category pc
JOIN categories c ON c.id = pc.category_id
WHERE c.id IN (90);

-- =====================================================
-- 12. GÁN PICKLEBALL ACCESSORIES (202)
-- =====================================================
INSERT IGNORE INTO product_category (product_id, category_id)
SELECT DISTINCT pc.product_id, 202
FROM product_category pc
JOIN categories c ON c.id = pc.category_id
WHERE c.id IN (92,102);

-- =====================================================
-- 13. GÁN PICKLEBALL BAGS (203)
-- =====================================================
INSERT IGNORE INTO product_category (product_id, category_id)
SELECT DISTINCT pc.product_id, 203
FROM product_category pc
JOIN categories c ON c.id = pc.category_id
WHERE c.id IN (91);
