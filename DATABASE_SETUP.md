# Database Setup Instructions

## MySQL Database Configuration

This application uses MySQL as the database. Follow these steps to set up the database:

### 1. Install MySQL

**macOS:**
```bash
brew install mysql
brew services start mysql
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get update
sudo apt-get install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
```

**Windows:**
Download and install MySQL from: https://dev.mysql.com/downloads/installer/

### 2. Create Database and User

Connect to MySQL as root:
```bash
mysql -u root -p
```

Run the following SQL commands:
```sql
-- Create database
CREATE DATABASE lms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user with caching_sha2_password (MySQL 8.0+ default)
CREATE USER 'lms_user'@'localhost' IDENTIFIED BY 'lms_password';

-- OR if you prefer mysql_native_password (for older clients):
-- CREATE USER 'lms_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'lms_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON lms_db.* TO 'lms_user'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;

-- Verify database creation
SHOW DATABASES;

-- Exit
EXIT;
```

### 3. Configure Application

The application is configured to use:
- **Database URL**: `jdbc:mysql://localhost:3306/lms_db`
- **Username**: `lms_user` (or set `DB_USERNAME` environment variable)
- **Password**: `lms_password` (or set `DB_PASSWORD` environment variable)
- **Port**: `3306` (default MySQL port)

You can override these values by setting environment variables:
```bash
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

Or update `application.yaml` directly.

### 4. Default Users

The application automatically creates two default users on startup:

**Teacher Account:**
- Email: `teacher@lms.com`
- Password: `teacher123`
- Role: TEACHER

**Student Account:**
- Email: `student@lms.com`
- Password: `student123`
- Role: STUDENT

**Important:** Change these passwords after first login in production!

### 5. Run the Application

After setting up MySQL, start the application:
```bash
mvn spring-boot:run
```

The application will:
1. Connect to MySQL
2. Create tables automatically (using `ddl-auto: update`)
3. Create default users (teacher and student)
4. Create sample courses (if they don't exist)

### 6. Connection String Details

The connection URL includes several important parameters:
- `createDatabaseIfNotExist=true` - Automatically creates the database if it doesn't exist
- `useSSL=false` - Disables SSL for local development (enable in production)
- `serverTimezone=UTC` - Sets timezone to UTC
- `allowPublicKeyRetrieval=true` - Allows retrieval of public key (needed for some MySQL versions)

### Troubleshooting

**Connection Error:**
- Ensure MySQL is running: `mysqladmin ping` or `systemctl status mysql`
- Check if the database exists: `mysql -u root -p -e "SHOW DATABASES;" | grep lms_db`
- Verify credentials in `application.yaml`
- Check MySQL port (default is 3306): `netstat -an | grep 3306`

**Permission Errors:**
- Ensure the user has proper privileges (see step 2)
- Try connecting manually: `mysql -u lms_user -p lms_db`

**Authentication Plugin Error (mysql_native_password not loaded):**
If you see "Plugin 'mysql_native_password' is not loaded" error, MySQL 8.0+ uses `caching_sha2_password` by default. The connection should work with the default authentication. If you still have issues, you can:

1. **Option 1 (Recommended):** Use the default `caching_sha2_password` - the connection URL already handles this
2. **Option 2:** Switch to `mysql_native_password` if needed:
  ```sql
  ALTER USER 'lms_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'lms_password';
  FLUSH PRIVILEGES;
  ```
3. **Option 3:** If using MySQL 8.0.4+, ensure the plugin is loaded:
  ```sql
  INSTALL PLUGIN mysql_native_password SONAME 'mysql_native_password.so';
  ```

**Port Already in Use:**
- Change MySQL port in `application.yaml` if 3306 is in use
- Or change MySQL port in `/etc/mysql/my.cnf` (Linux) or `/usr/local/etc/my.cnf` (macOS)

**Character Encoding Issues:**
- The database is created with `utf8mb4` character set to support emojis and special characters
- If you see encoding issues, verify the database charset:
  ```sql
  SHOW CREATE DATABASE lms_db;
  ```

**Access Denied Error:**
- Make sure the user has been granted privileges
- Try resetting the password:
  ```sql
  ALTER USER 'lms_user'@'localhost' IDENTIFIED BY 'lms_password';
  FLUSH PRIVILEGES;
  ```
