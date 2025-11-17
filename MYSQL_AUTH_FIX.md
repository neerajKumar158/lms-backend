# MySQL Authentication Plugin Fix

## Error: "Plugin 'mysql_native_password' is not loaded"

This error occurs when MySQL 8.0+ tries to use the `mysql_native_password` authentication plugin, but it's not available or the user was created with a different authentication method.

## Quick Fix (Recommended)

**Run this SQL script to fix the issue:**

```bash
mysql -u root -p < fix_mysql_user.sql
```

Or manually run these commands:

```sql
mysql -u root -p

DROP USER IF EXISTS 'lms_user'@'localhost';
CREATE USER 'lms_user'@'localhost' IDENTIFIED BY 'lms_password';
GRANT ALL PRIVILEGES ON lms_db.* TO 'lms_user'@'localhost';
FLUSH PRIVILEGES;
```

This will recreate the user with the default `caching_sha2_password` authentication, which works with MySQL Connector/J.

## Solution

### Option 1: Use Default Authentication (Recommended)

MySQL 8.0+ uses `caching_sha2_password` by default. Recreate the user with the default authentication:

```sql
-- Connect to MySQL as root
mysql -u root -p

-- Drop existing user if it exists
DROP USER IF EXISTS 'lms_user'@'localhost';

-- Create user with default authentication (caching_sha2_password)
CREATE USER 'lms_user'@'localhost' IDENTIFIED BY 'lms_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON lms_db.* TO 'lms_user'@'localhost';
FLUSH PRIVILEGES;

-- Verify
SELECT user, host, plugin FROM mysql.user WHERE user = 'lms_user';
```

The user should show `caching_sha2_password` as the plugin.

### Option 2: Use mysql_native_password (If needed)

If you specifically need `mysql_native_password`, you can:

1. **Install the plugin** (if not already installed):
```sql
INSTALL PLUGIN mysql_native_password SONAME 'mysql_native_password.so';
```

2. **Alter the user**:
```sql
ALTER USER 'lms_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'lms_password';
FLUSH PRIVILEGES;
```

### Option 3: Check Current User Authentication

First, check what authentication method your user is using:

```sql
SELECT user, host, plugin FROM mysql.user WHERE user = 'lms_user';
```

If it shows `mysql_native_password` but the plugin isn't loaded, use Option 1 or Option 2 above.

## Quick Fix Script

Run this complete script to fix the issue:

```sql
-- Connect as root
mysql -u root -p

-- Drop and recreate user with default authentication
DROP USER IF EXISTS 'lms_user'@'localhost';
CREATE USER 'lms_user'@'localhost' IDENTIFIED BY 'lms_password';
GRANT ALL PRIVILEGES ON lms_db.* TO 'lms_user'@'localhost';
FLUSH PRIVILEGES;

-- Verify
SELECT user, host, plugin FROM mysql.user WHERE user = 'lms_user';
```

The output should show `caching_sha2_password` as the plugin.

## After Fixing

Restart your Spring Boot application:
```bash
mvn spring-boot:run
```

The connection should now work properly.

