-- Initial database setup script
-- This script runs automatically when the database container is first created

-- Create database if it doesn't exist (usually already created by MYSQL_DATABASE env var)
CREATE DATABASE IF NOT EXISTS teckiz CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE teckiz;

-- Set timezone
SET time_zone = '+00:00';

-- Create a user for the application (if not already created by MYSQL_USER env var)
-- CREATE USER IF NOT EXISTS 'teckiz'@'%' IDENTIFIED BY 'teckizpassword';
-- GRANT ALL PRIVILEGES ON teckiz.* TO 'teckiz'@'%';
-- FLUSH PRIVILEGES;

