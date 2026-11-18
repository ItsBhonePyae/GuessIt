-- GuessIt database schema (MySQL / MariaDB)
-- Run once against your server:  mysql -u root -p < init.sql

CREATE DATABASE IF NOT EXISTS guessit
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE guessit;

CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(120)  NOT NULL,
    email         VARCHAR(180)  NOT NULL UNIQUE,
    username      VARCHAR(60)   NOT NULL UNIQUE,
    password_hash VARCHAR(120)  NOT NULL,
    role          VARCHAR(20)   NOT NULL DEFAULT 'PLAYER',
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS remember_tokens (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT           NOT NULL,
    token_hash  VARCHAR(120)  NOT NULL,
    expires_at  DATETIME      NOT NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_remember_user (user_id),
    INDEX idx_remember_expires (expires_at)
) ENGINE=InnoDB;
