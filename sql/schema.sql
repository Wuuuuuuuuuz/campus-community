-- ============================================================
-- Database: campus_community
-- Charset: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_community
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus_community;

-- -----------------------------------------------------------
-- Table: user
-- -----------------------------------------------------------
CREATE TABLE `user` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `username`      VARCHAR(50)   NOT NULL COMMENT 'Login username',
  `password`      VARCHAR(255)  NOT NULL COMMENT 'BCrypt encoded password',
  `nickname`      VARCHAR(50)   DEFAULT NULL COMMENT 'Display name',
  `email`         VARCHAR(100)  DEFAULT NULL COMMENT 'Email address',
  `avatar`        VARCHAR(500)  DEFAULT NULL COMMENT 'Avatar image URL',
  `phone`         VARCHAR(20)   DEFAULT NULL COMMENT 'Phone number',
  `role`          VARCHAR(20)   NOT NULL DEFAULT 'USER' COMMENT 'Role: USER, ADMIN',
  `status`        TINYINT       NOT NULL DEFAULT 1 COMMENT '0=disabled, 1=active',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User table';

-- -----------------------------------------------------------
-- Table: category
-- -----------------------------------------------------------
CREATE TABLE `category` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `name`          VARCHAR(50)   NOT NULL COMMENT 'Category name',
  `description`   VARCHAR(200)  DEFAULT NULL COMMENT 'Category description',
  `sort_order`    INT           NOT NULL DEFAULT 0 COMMENT 'Display sort order',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Post category table';

-- -----------------------------------------------------------
-- Table: post
-- -----------------------------------------------------------
CREATE TABLE `post` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `title`         VARCHAR(200)  NOT NULL COMMENT 'Post title',
  `content`       LONGTEXT      NOT NULL COMMENT 'Post body (markdown)',
  `summary`       VARCHAR(500)  DEFAULT NULL COMMENT 'Summary for list display',
  `user_id`       BIGINT        NOT NULL COMMENT 'Author user ID',
  `category_id`   BIGINT        DEFAULT NULL COMMENT 'Category ID',
  `view_count`    INT           NOT NULL DEFAULT 0 COMMENT 'View count',
  `like_count`    INT           NOT NULL DEFAULT 0 COMMENT 'Like count',
  `comment_count` INT           NOT NULL DEFAULT 0 COMMENT 'Comment count',
  `status`        TINYINT       NOT NULL DEFAULT 1 COMMENT '0=draft, 1=published, 2=hidden',
  `is_pinned`     TINYINT       NOT NULL DEFAULT 0 COMMENT '0=no, 1=pinned to top',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  PRIMARY KEY (`id`),
  KEY `idx_post_user_id` (`user_id`),
  KEY `idx_post_category_id` (`category_id`),
  KEY `idx_post_status` (`status`),
  KEY `idx_post_created_at` (`created_at`),
  KEY `idx_post_is_pinned` (`is_pinned`),
  FULLTEXT KEY `ft_post_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Post table';

-- -----------------------------------------------------------
-- Table: comment
-- -----------------------------------------------------------
CREATE TABLE `comment` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `content`           TEXT          NOT NULL COMMENT 'Comment body',
  `post_id`           BIGINT        NOT NULL COMMENT 'Associated post ID',
  `user_id`           BIGINT        NOT NULL COMMENT 'Comment author user ID',
  `parent_id`         BIGINT        DEFAULT NULL COMMENT 'Parent comment ID, NULL = top-level',
  `reply_to_user_id`  BIGINT        DEFAULT NULL COMMENT 'The user being replied to',
  `like_count`        INT           NOT NULL DEFAULT 0 COMMENT 'Like count',
  `status`            TINYINT       NOT NULL DEFAULT 1 COMMENT '0=deleted, 1=active',
  `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  PRIMARY KEY (`id`),
  KEY `idx_comment_post_id` (`post_id`),
  KEY `idx_comment_user_id` (`user_id`),
  KEY `idx_comment_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Comment table';

-- ============================================================
-- Seed data
-- ============================================================
INSERT INTO `category` (`name`, `description`, `sort_order`) VALUES
('Campus News',   'Official announcements, events, and campus updates', 1),
('Academic',      'Course discussions, study groups, academic resources', 2),
('Life',          'Dorm life, dining, activities, daily campus life', 3),
('Job & Career',  'Internships, job postings, career advice', 4),
('Marketplace',   'Buy, sell, trade items among students', 5),
('Tech & Geek',   'Programming, gadgets, tech discussions', 6);

-- Default admin: username=admin, password=admin123
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 'System Admin', 'ADMIN');
