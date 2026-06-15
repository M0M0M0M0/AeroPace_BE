-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 15, 2026 at 05:41 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `aeropace`
--

-- --------------------------------------------------------

--
-- Table structure for table `brands`
--

CREATE TABLE `brands` (
                          `id` int(11) NOT NULL,
                          `name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `cart_items`
--

CREATE TABLE `cart_items` (
                              `id` int(11) NOT NULL,
                              `product_variant_id` int(11) NOT NULL,
                              `quantity` int(11) NOT NULL,
                              `created_at` datetime DEFAULT current_timestamp(),
                              `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
                              `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
                              `id` int(11) NOT NULL,
                              `name` varchar(50) NOT NULL,
                              `description` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `comments`
--

CREATE TABLE `comments` (
                            `id` int(11) NOT NULL,
                            `user_id` int(11) NOT NULL,
                            `product_id` int(11) NOT NULL,
                            `content` varchar(500) NOT NULL,
                            `created_at` datetime DEFAULT current_timestamp(),
                            `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `customer_profiles`
--

CREATE TABLE `customer_profiles` (
                                     `id` int(11) NOT NULL,
                                     `user_id` int(11) NOT NULL,
                                     `full_name` varchar(150) NOT NULL,
                                     `address` varchar(500) DEFAULT NULL,
                                     `dob` date DEFAULT NULL,
                                     `phone_number` varchar(20) DEFAULT NULL,
                                     `gender` varchar(20) DEFAULT NULL,
                                     `created_at` datetime DEFAULT current_timestamp(),
                                     `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
                                     `ward` varchar(150) DEFAULT NULL,
                                     `district` varchar(150) DEFAULT NULL,
                                     `province` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `historical_products`
--

CREATE TABLE `historical_products` (
                                       `id` bigint(20) NOT NULL,
                                       `product_id` bigint(20) NOT NULL,
                                       `name` varchar(200) DEFAULT NULL,
                                       `description` longtext DEFAULT NULL,
                                       `brand` varchar(200) DEFAULT NULL,
                                       `option1_name` varchar(50) DEFAULT NULL,
                                       `option2_name` varchar(50) DEFAULT NULL,
                                       `option3_name` varchar(50) DEFAULT NULL,
                                       `valid_from` datetime(3) NOT NULL,
                                       `valid_to` datetime(3) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `historical_product_images`
--

CREATE TABLE `historical_product_images` (
                                             `id` bigint(20) NOT NULL,
                                             `historical_product_id` bigint(20) NOT NULL,
                                             `image_url` varchar(500) NOT NULL,
                                             `position` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `historical_product_variants`
--

CREATE TABLE `historical_product_variants` (
                                               `id` bigint(20) NOT NULL,
                                               `historical_product_id` bigint(20) NOT NULL,
                                               `original_variant_id` bigint(20) DEFAULT NULL,
                                               `sku` varchar(100) DEFAULT NULL,
                                               `price` decimal(19,2) NOT NULL,
                                               `compare_price` decimal(19,2) DEFAULT NULL,
                                               `option1_value` varchar(50) DEFAULT NULL,
                                               `option2_value` varchar(50) DEFAULT NULL,
                                               `option3_value` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `inventory_items`
--

CREATE TABLE `inventory_items` (
                                   `id` bigint(20) NOT NULL,
                                   `product_variant_id` bigint(20) NOT NULL,
                                   `sku` varchar(100) DEFAULT NULL,
                                   `cached_stock` int(11) NOT NULL DEFAULT 0,
                                   `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
                          `id` int(11) NOT NULL,
                          `user_id` int(11) NOT NULL,
                          `total_price` decimal(12,2) NOT NULL,
                          `status` varchar(20) NOT NULL,
                          `shipping_address` varchar(500) NOT NULL,
                          `phone_number` varchar(20) NOT NULL,
                          `created_at` datetime DEFAULT current_timestamp(),
                          `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
                          `receiver_name` varchar(255) DEFAULT NULL,
                          `note` varchar(500) DEFAULT NULL,
                          `shipping_method` varchar(50) NOT NULL DEFAULT 'STANDARD',
                          `cancel_type` text DEFAULT NULL,
                          `shipping_fee` decimal(15,2) NOT NULL DEFAULT 0.00,
                          `ward` varchar(100) DEFAULT NULL,
                          `district` varchar(100) DEFAULT NULL,
                          `province` varchar(100) DEFAULT NULL,
                          `payment_method` varchar(50) DEFAULT NULL,
                          `payment_status` varchar(30) NOT NULL DEFAULT 'PENDING',
                          `payment_order_id` varchar(100) DEFAULT NULL,
                          `payment_transaction_id` varchar(100) DEFAULT NULL,
                          `refund_reason` text DEFAULT NULL,
                          `vat` decimal(12,2) NOT NULL DEFAULT 0.00,
                          `order_code` varchar(30) DEFAULT NULL,
                          `cancel_reason` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `order_items`
--

CREATE TABLE `order_items` (
                               `id` int(11) NOT NULL,
                               `order_id` int(11) NOT NULL,
                               `product_variant_id` int(11) NOT NULL,
                               `quantity` int(11) NOT NULL,
                               `price` decimal(10,2) NOT NULL,
                               `product_name` varchar(255) NOT NULL DEFAULT '',
                               `variant_name` varchar(255) NOT NULL DEFAULT '',
                               `sku` varchar(100) NOT NULL DEFAULT '',
                               `product_img_url` varchar(500) DEFAULT NULL,
                               `note` varchar(500) DEFAULT NULL,
                               `brand_name` varchar(200) DEFAULT NULL,
                               `product_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `posts`
--

CREATE TABLE `posts` (
                         `id` int(11) NOT NULL,
                         `title` varchar(255) NOT NULL,
                         `slug` varchar(300) NOT NULL,
                         `content` text DEFAULT NULL,
                         `thumbnail` varchar(500) DEFAULT NULL,
                         `user_id` int(11) NOT NULL,
                         `created_at` datetime DEFAULT current_timestamp(),
                         `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
                            `id` int(11) NOT NULL,
                            `name` varchar(200) NOT NULL,
                            `slug` varchar(300) NOT NULL,
                            `description` longtext DEFAULT NULL,
                            `brand_id` int(11) DEFAULT NULL,
                            `option1_name` varchar(50) DEFAULT NULL,
                            `option2_name` varchar(50) DEFAULT NULL,
                            `option3_name` varchar(50) DEFAULT NULL,
                            `created_at` datetime DEFAULT current_timestamp(),
                            `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
                            `status` enum('ACTIVE','DRAFT','ARCHIVED','DELETED') DEFAULT NULL,
                            `average_rating` decimal(2,1) DEFAULT 0.0,
                            `review_count` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `product_category`
--

CREATE TABLE `product_category` (
                                    `product_id` int(11) NOT NULL,
                                    `category_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `product_images`
--

CREATE TABLE `product_images` (
                                  `id` int(11) NOT NULL,
                                  `product_id` int(11) NOT NULL,
                                  `image_url` varchar(500) NOT NULL,
                                  `position` int(11) NOT NULL,
                                  `created_at` datetime DEFAULT current_timestamp(),
                                  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `product_variants`
--

CREATE TABLE `product_variants` (
                                    `id` int(11) NOT NULL,
                                    `product_id` int(11) NOT NULL,
                                    `option1_value` varchar(50) DEFAULT NULL,
                                    `option2_value` varchar(50) DEFAULT NULL,
                                    `option3_value` varchar(50) DEFAULT NULL,
                                    `price` decimal(10,2) NOT NULL,
                                    `compare_price` decimal(10,2) DEFAULT NULL,
                                    `sku` varchar(100) DEFAULT NULL,
                                    `created_at` datetime DEFAULT current_timestamp(),
                                    `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
                                    `is_deleted` tinyint(1) DEFAULT 0,
                                    `active_variant` tinyint(4) GENERATED ALWAYS AS (case when `is_deleted` = 0 then 1 else NULL end) STORED
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `reviews`
--

CREATE TABLE `reviews` (
                           `id` int(11) NOT NULL,
                           `user_id` int(11) NOT NULL,
                           `product_id` int(11) NOT NULL,
                           `variant_id` int(11) NOT NULL,
                           `order_id` int(11) NOT NULL,
                           `rating` decimal(2,1) DEFAULT NULL,
                           `comment` text DEFAULT NULL,
                           `image_urls` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`image_urls`)),
                           `status` enum('PENDING','ACTIVE','EDITED','DELETED','EXPIRED') NOT NULL DEFAULT 'PENDING',
                           `note` text DEFAULT NULL,
                           `parent_id` int(11) DEFAULT NULL,
                           `created_at` datetime NOT NULL DEFAULT current_timestamp(),
                           `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
                           `unique_active_key` varchar(100) GENERATED ALWAYS AS (case when `status` in ('ACTIVE','PENDING') then concat(cast(`order_id` as char charset utf8mb4),'-',cast(`product_id` as char charset utf8mb4)) else NULL end) VIRTUAL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE `roles` (
                         `id` int(11) NOT NULL,
                         `name` varchar(20) NOT NULL,
                         `description` varchar(255) DEFAULT NULL,
                         `created_at` datetime DEFAULT current_timestamp(),
                         `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `shipping_methods`
--

CREATE TABLE `shipping_methods` (
                                    `id` int(11) NOT NULL,
                                    `name` varchar(100) NOT NULL,
                                    `fee` decimal(10,2) NOT NULL,
                                    `status` enum('ACTIVE','DISABLE') NOT NULL DEFAULT 'ACTIVE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `stock_movements`
--

CREATE TABLE `stock_movements` (
                                   `id` bigint(20) NOT NULL,
                                   `inventory_item_id` bigint(20) NOT NULL,
                                   `movement_type` enum('RESTOCK','SALE','CANCEL','ADJUSTMENT') NOT NULL,
                                   `quantity` int(11) NOT NULL,
                                   `reference_order_id` bigint(20) DEFAULT NULL,
                                   `note` varchar(255) DEFAULT NULL,
                                   `created_at` datetime(3) DEFAULT current_timestamp(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
                         `id` int(11) NOT NULL,
                         `username` varchar(50) NOT NULL,
                         `password` varchar(255) NOT NULL,
                         `email` varchar(255) NOT NULL,
                         `role_id` int(11) NOT NULL,
                         `created_at` datetime DEFAULT current_timestamp(),
                         `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
                         `status` varchar(20) NOT NULL DEFAULT 'ACTIVE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `brands`
--
ALTER TABLE `brands`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `cart_items`
--
ALTER TABLE `cart_items`
    ADD PRIMARY KEY (`id`),
  ADD KEY `product_variant_id` (`product_variant_id`),
  ADD KEY `fk_user_id` (`user_id`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `comments`
--
ALTER TABLE `comments`
    ADD PRIMARY KEY (`id`),
  ADD KEY `idx_comments_product` (`product_id`),
  ADD KEY `idx_comments_user` (`user_id`);

--
-- Indexes for table `customer_profiles`
--
ALTER TABLE `customer_profiles`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`);

--
-- Indexes for table `historical_products`
--
ALTER TABLE `historical_products`
    ADD PRIMARY KEY (`id`),
  ADD KEY `idx_hp_product_valid` (`product_id`,`valid_from`,`valid_to`);

--
-- Indexes for table `historical_product_images`
--
ALTER TABLE `historical_product_images`
    ADD PRIMARY KEY (`id`),
  ADD KEY `idx_hpi_hp` (`historical_product_id`);

--
-- Indexes for table `historical_product_variants`
--
ALTER TABLE `historical_product_variants`
    ADD PRIMARY KEY (`id`),
  ADD KEY `idx_hpv_hp` (`historical_product_id`);

--
-- Indexes for table `inventory_items`
--
ALTER TABLE `inventory_items`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `product_variant_id` (`product_variant_id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `order_code` (`order_code`),
  ADD KEY `idx_orders_user` (`user_id`);

--
-- Indexes for table `order_items`
--
ALTER TABLE `order_items`
    ADD PRIMARY KEY (`id`),
  ADD KEY `product_variant_id` (`product_variant_id`),
  ADD KEY `idx_order_items_order` (`order_id`);

--
-- Indexes for table `posts`
--
ALTER TABLE `posts`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `slug` (`slug`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `slug` (`slug`),
  ADD KEY `brand_id` (`brand_id`);

--
-- Indexes for table `product_category`
--
ALTER TABLE `product_category`
    ADD PRIMARY KEY (`product_id`,`category_id`),
  ADD KEY `category_id` (`category_id`);

--
-- Indexes for table `product_images`
--
ALTER TABLE `product_images`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `product_id` (`product_id`,`position`),
  ADD KEY `idx_product_images_product` (`product_id`);

--
-- Indexes for table `product_variants`
--
ALTER TABLE `product_variants`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_variant_active` (`product_id`,`option1_value`,`option2_value`,`option3_value`,`active_variant`),
  ADD KEY `idx_variant_product` (`product_id`);

--
-- Indexes for table `reviews`
--
ALTER TABLE `reviews`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_active_pending_order_product` (`unique_active_key`),
  ADD KEY `fk_review_variant` (`variant_id`),
  ADD KEY `idx_reviews_product_status` (`product_id`,`status`),
  ADD KEY `idx_reviews_user` (`user_id`),
  ADD KEY `idx_reviews_parent` (`parent_id`),
  ADD KEY `idx_reviews_order_product` (`order_id`,`product_id`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `shipping_methods`
--
ALTER TABLE `shipping_methods`
    ADD PRIMARY KEY (`id`);

--
-- Indexes for table `stock_movements`
--
ALTER TABLE `stock_movements`
    ADD PRIMARY KEY (`id`),
  ADD KEY `idx_sm_inventory` (`inventory_item_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `role_id` (`role_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `brands`
--
ALTER TABLE `brands`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `cart_items`
--
ALTER TABLE `cart_items`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `comments`
--
ALTER TABLE `comments`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `customer_profiles`
--
ALTER TABLE `customer_profiles`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `historical_products`
--
ALTER TABLE `historical_products`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `historical_product_images`
--
ALTER TABLE `historical_product_images`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `historical_product_variants`
--
ALTER TABLE `historical_product_variants`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `inventory_items`
--
ALTER TABLE `inventory_items`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `order_items`
--
ALTER TABLE `order_items`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `posts`
--
ALTER TABLE `posts`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `product_images`
--
ALTER TABLE `product_images`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `product_variants`
--
ALTER TABLE `product_variants`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `reviews`
--
ALTER TABLE `reviews`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `roles`
--
ALTER TABLE `roles`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `shipping_methods`
--
ALTER TABLE `shipping_methods`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `stock_movements`
--
ALTER TABLE `stock_movements`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `cart_items`
--
ALTER TABLE `cart_items`
    ADD CONSTRAINT `fk_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `comments`
--
ALTER TABLE `comments`
    ADD CONSTRAINT `comments_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `comments_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

--
-- Constraints for table `customer_profiles`
--
ALTER TABLE `customer_profiles`
    ADD CONSTRAINT `customer_profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
    ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `order_items`
--
ALTER TABLE `order_items`
    ADD CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`);

--
-- Constraints for table `posts`
--
ALTER TABLE `posts`
    ADD CONSTRAINT `posts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `products`
--
ALTER TABLE `products`
    ADD CONSTRAINT `products_ibfk_1` FOREIGN KEY (`brand_id`) REFERENCES `brands` (`id`);

--
-- Constraints for table `product_category`
--
ALTER TABLE `product_category`
    ADD CONSTRAINT `product_category_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  ADD CONSTRAINT `product_category_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`);

--
-- Constraints for table `product_images`
--
ALTER TABLE `product_images`
    ADD CONSTRAINT `product_images_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

--
-- Constraints for table `product_variants`
--
ALTER TABLE `product_variants`
    ADD CONSTRAINT `product_variants_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

--
-- Constraints for table `reviews`
--
ALTER TABLE `reviews`
    ADD CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  ADD CONSTRAINT `fk_review_parent` FOREIGN KEY (`parent_id`) REFERENCES `reviews` (`id`),
  ADD CONSTRAINT `fk_review_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  ADD CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `fk_review_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`);

--
-- Constraints for table `users`
--
ALTER TABLE `users`
    ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
