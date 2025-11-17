-- MySQL User Fix Script
-- Run this script as MySQL root user to fix the authentication issue
-- Usage: mysql -u root -p < fix_mysql_user.sql

-- Drop existing user if it exists
DROP USER IF EXISTS 'lms_user'@'localhost';

-- Create user with default authentication (caching_sha2_password for MySQL 8.0+)
-- This is the default and works with MySQL Connector/J
CREATE USER 'lms_user'@'localhost' IDENTIFIED BY 'lms_password';

-- Grant all privileges on the database
GRANT ALL PRIVILEGES ON lms_db.* TO 'lms_user'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;

-- Verify the user was created correctly
SELECT user, host, plugin, authentication_string FROM mysql.user WHERE user = 'lms_user';

-- Show current authentication plugin (should be caching_sha2_password)
SHOW VARIABLES LIKE 'default_authentication_plugin';




