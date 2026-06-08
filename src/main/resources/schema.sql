DROP TABLE IF EXISTS `schedule`;

CREATE TABLE `schedule` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `start_date` DATETIME NOT NULL,
    `end_date` DATETIME NOT NULL,
    `category` VARCHAR(20),
    `status` VARCHAR(20),
    `all_day` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `work_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL,
    `content` VARCHAR(2000),
    `start_at` TIMESTAMP NOT NULL,
    `end_at` TIMESTAMP NOT NULL,
    `all_day` BOOLEAN NOT NULL,
    `project_name` VARCHAR(255),
    `category` VARCHAR(100),
    `status` VARCHAR(100),
    `progress_rate` INT,
    `report_display_type` VARCHAR(100),
    `report_included` BOOLEAN NOT NULL DEFAULT TRUE
);
