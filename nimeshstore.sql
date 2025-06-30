-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.30 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.8.0.6908
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for nimeshstore
CREATE DATABASE IF NOT EXISTS `nimeshstore` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `nimeshstore`;

-- Dumping structure for table nimeshstore.activity_logs
CREATE TABLE IF NOT EXISTS `activity_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) NOT NULL,
  `activityType` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `timestamp` datetime NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `reference_id` bigint DEFAULT NULL,
  `reference_type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=425 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.categories
CREATE TABLE IF NOT EXISTS `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_t8o6pivur7nn124jehx7cygw5` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.credit_accounts
CREATE TABLE IF NOT EXISTS `credit_accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `balance` decimal(38,2) NOT NULL,
  `credit_limit` decimal(38,2) DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_1ygyw3601l6acsht12lq857ad` (`customer_id`),
  CONSTRAINT `FKf1jn0lsi2xkarpbon30voiao3` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.customers
CREATE TABLE IF NOT EXISTS `customers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `contact_no` varchar(255) DEFAULT NULL,
  `customer_type` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.invoices
CREATE TABLE IF NOT EXISTS `invoices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_type` varchar(255) NOT NULL,
  `date` datetime(6) NOT NULL,
  `discount_amount` decimal(38,2) DEFAULT NULL,
  `discount_percentage` decimal(38,2) DEFAULT NULL,
  `final_amount` decimal(38,2) NOT NULL,
  `invoice_number` varchar(255) DEFAULT NULL,
  `payment_method` varchar(255) NOT NULL,
  `payment_status` varchar(255) DEFAULT NULL,
  `total_amount` decimal(38,2) NOT NULL,
  `customer_id` bigint DEFAULT NULL,
  `cash_received` decimal(38,2) DEFAULT NULL,
  `change_amount` decimal(38,2) DEFAULT NULL,
  `item_discounts_total` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_l1x55mfsay7co0r3m9ynvipd5` (`invoice_number`),
  KEY `FKq2w4hmh6l9othnp6cepp0cfe2` (`customer_id`),
  CONSTRAINT `FKq2w4hmh6l9othnp6cepp0cfe2` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=149 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.invoice_items
CREATE TABLE IF NOT EXISTS `invoice_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` decimal(38,2) NOT NULL,
  `total` decimal(38,2) NOT NULL,
  `unit_price` decimal(38,2) NOT NULL,
  `invoice_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  `discount_amount` decimal(38,2) DEFAULT NULL,
  `buying_price` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK46ae0lhu1oqs7cv91fn6y9n7w` (`invoice_id`),
  KEY `FKs3tu9gmkgshq8oeq5n0rinxeu` (`product_id`),
  CONSTRAINT `FK46ae0lhu1oqs7cv91fn6y9n7w` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`),
  CONSTRAINT `FKs3tu9gmkgshq8oeq5n0rinxeu` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=349 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.invoice_item_batches
CREATE TABLE IF NOT EXISTS `invoice_item_batches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `invoice_item_id` bigint NOT NULL,
  `product_batch_id` bigint NOT NULL,
  `quantity` decimal(38,2) NOT NULL,
  `unit_cost` decimal(38,2) NOT NULL,
  `unit_price` decimal(38,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_invoice_item_batch_invoice_item` (`invoice_item_id`),
  KEY `idx_invoice_item_batch_product_batch` (`product_batch_id`),
  CONSTRAINT `fk_invoice_item_batch_invoice_item` FOREIGN KEY (`invoice_item_id`) REFERENCES `invoice_items` (`id`),
  CONSTRAINT `fk_invoice_item_batch_product_batch` FOREIGN KEY (`product_batch_id`) REFERENCES `product_batches` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.order_items
CREATE TABLE IF NOT EXISTS `order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` decimal(38,2) NOT NULL,
  `total` decimal(38,2) NOT NULL,
  `unit_price` decimal(38,2) NOT NULL,
  `product_id` bigint DEFAULT NULL,
  `purchase_order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKocimc7dtr037rh4ls4l95nlfi` (`product_id`),
  KEY `FK9gcbbi81bpyllq9ucilobvk8u` (`purchase_order_id`),
  CONSTRAINT `FK9gcbbi81bpyllq9ucilobvk8u` FOREIGN KEY (`purchase_order_id`) REFERENCES `purchase_orders` (`id`),
  CONSTRAINT `FKocimc7dtr037rh4ls4l95nlfi` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.pre_orders
CREATE TABLE IF NOT EXISTS `pre_orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_number` varchar(255) DEFAULT NULL,
  `order_date` datetime(6) NOT NULL,
  `status` varchar(50) NOT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_pre_order_number` (`order_number`),
  KEY `FK_pre_order_customer` (`customer_id`),
  CONSTRAINT `FK_pre_order_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.pre_order_items
CREATE TABLE IF NOT EXISTS `pre_order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` decimal(38,2) NOT NULL,
  `unit_price` decimal(38,2) NOT NULL,
  `total` decimal(38,2) NOT NULL,
  `product_id` bigint NOT NULL,
  `pre_order_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_pre_order_item_product` (`product_id`),
  KEY `FK_pre_order_item_pre_order` (`pre_order_id`),
  CONSTRAINT `FK_pre_order_item_pre_order` FOREIGN KEY (`pre_order_id`) REFERENCES `pre_orders` (`id`),
  CONSTRAINT `FK_pre_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.pre_order_notifications
CREATE TABLE IF NOT EXISTS `pre_order_notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message` varchar(500) NOT NULL,
  `created_date` datetime(6) NOT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `pre_order_id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_notification_pre_order` (`pre_order_id`),
  KEY `FK_notification_customer` (`customer_id`),
  CONSTRAINT `FK_notification_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`),
  CONSTRAINT `FK_notification_pre_order` FOREIGN KEY (`pre_order_id`) REFERENCES `pre_orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.products
CREATE TABLE IF NOT EXISTS `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `barcode` varchar(255) DEFAULT NULL,
  `buying_price` decimal(38,2) NOT NULL,
  `current_stock` decimal(10,2) NOT NULL,
  `date_added` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `reorder_level` decimal(10,2) DEFAULT NULL,
  `selling_price` decimal(38,2) NOT NULL,
  `wholesale_price` decimal(38,2) DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `unit_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_qfr8vf85k3q1xinifvsl1eynf` (`barcode`),
  KEY `FKog2rp4qthbtt2lfyhfo32lsw9` (`category_id`),
  KEY `FKeex0i50vfsa5imebrfdiyhmp9` (`unit_id`),
  CONSTRAINT `FKeex0i50vfsa5imebrfdiyhmp9` FOREIGN KEY (`unit_id`) REFERENCES `units` (`id`),
  CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.product_batches
CREATE TABLE IF NOT EXISTS `product_batches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `batch_number` varchar(50) NOT NULL,
  `purchase_date` datetime(6) NOT NULL,
  `expiry_date` datetime(6) DEFAULT NULL,
  `buying_price` decimal(38,2) NOT NULL,
  `selling_price` decimal(38,2) NOT NULL,
  `wholesale_price` decimal(38,2) DEFAULT NULL,
  `initial_quantity` decimal(38,2) NOT NULL,
  `remaining_quantity` decimal(38,2) NOT NULL,
  `supplier_reference` varchar(255) DEFAULT NULL,
  `purchase_order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_product_batch_product` (`product_id`),
  KEY `idx_product_batch_purchase_order` (`purchase_order_id`),
  KEY `idx_product_batch_remaining` (`product_id`,`remaining_quantity`),
  KEY `idx_product_batch_purchase_date` (`purchase_date`),
  KEY `idx_product_batch_expiry_date` (`expiry_date`),
  CONSTRAINT `fk_product_batch_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_product_batch_purchase_order` FOREIGN KEY (`purchase_order_id`) REFERENCES `purchase_orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=136 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.purchase_orders
CREATE TABLE IF NOT EXISTS `purchase_orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `delivery_date` datetime(6) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `order_date` datetime(6) NOT NULL,
  `order_number` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  `supplier_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_nqsdqb8p2iobsmeaa2jxxw7k` (`order_number`),
  KEY `FKrpdasmb8y8xs5tiy4369xpinq` (`supplier_id`),
  CONSTRAINT `FKrpdasmb8y8xs5tiy4369xpinq` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.sms_notifications
CREATE TABLE IF NOT EXISTS `sms_notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `error_message` varchar(500) DEFAULT NULL,
  `external_id` varchar(255) DEFAULT NULL,
  `message` varchar(500) NOT NULL,
  `phone_number` varchar(255) NOT NULL,
  `sent_date` datetime(6) NOT NULL,
  `status` varchar(255) NOT NULL,
  `customer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqajqp22h8o9dlincn03bua9x5` (`customer_id`),
  CONSTRAINT `FKqajqp22h8o9dlincn03bua9x5` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.suppliers
CREATE TABLE IF NOT EXISTS `suppliers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `contact_no` varchar(255) DEFAULT NULL,
  `contact_person` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.units
CREATE TABLE IF NOT EXISTS `units` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversion_factor` decimal(38,2) DEFAULT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_etw07nfppovq9p7ov8hcb38wy` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table nimeshstore.users
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `userType` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
