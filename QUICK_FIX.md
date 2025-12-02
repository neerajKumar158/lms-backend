# Quick Fix for MySQL Authentication Error

## Problem
Error: `Plugin 'mysql_native_password' is not loaded`

## Solution (2 minutes)

### Step 1: Run the fix script

```bash
mysql -u root -p < fix_mysql_user.sql
```

When prompted, enter your MySQL root password.

### Step 2: Verify the fix

```bash
mysql -u root -p -e "SELECT user, host, plugin FROM mysql.user WHERE user = 'lms_user';"
```

You should see `caching_sha2_password` as the plugin (not `mysql_native_password`).

### Step 3: Restart the application

```bash
mvn spring-boot:run
```

## Manual Fix (if script doesn't work)

Connect to MySQL and run:

```sql
mysql -u root -p

-- Then run these commands:
DROP USER IF EXISTS 'lms_user'@'localhost';
CREATE USER 'lms_user'@'localhost' IDENTIFIED BY 'lms_password';
GRANT ALL PRIVILEGES ON lms_db.* TO 'lms_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

## Why This Works

MySQL 8.0+ uses `caching_sha2_password` by default, which is fully supported by the MySQL Connector/J driver. The error occurs when a user was created with `mysql_native_password` but that plugin isn't available in your MySQL installation. Recreating the user with the default authentication method resolves this.







