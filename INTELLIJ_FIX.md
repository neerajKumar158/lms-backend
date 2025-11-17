# Fix IntelliJ ExceptionInInitializerError

## Quick Fix Steps:

### 1. Invalidate IntelliJ Caches
- Go to: **File → Invalidate Caches...**
- Check: "Clear file system cache and Local History"
- Click: **Invalidate and Restart**

### 2. Check Java Version in IntelliJ
- Go to: **File → Project Structure (⌘; on Mac, Ctrl+Alt+Shift+S on Windows)**
- Under **Project**:
  - Project SDK: Should be **Java 21**
  - Project language level: Should be **21 - Record patterns, pattern matching for switch, etc.**
- Under **Modules**:
  - Select your module
  - Language level: **21**

### 3. Check Maven Settings
- Go to: **File → Settings → Build, Execution, Deployment → Build Tools → Maven**
- Maven home directory: Should point to your Maven installation
- User settings file: Should be valid
- Click **Apply**

### 4. Reimport Maven Project
- Right-click on `pom.xml` in the project
- Select: **Maven → Reload Project**
- Or: **Maven → Generate Sources and Update Folders**

### 5. Rebuild Project
- Go to: **Build → Rebuild Project**

### 6. If Still Not Working - Manual Steps:

#### Delete IntelliJ Cache:
```bash
cd /Users/ent00398/New1/lms-backend
rm -rf .idea
```

Then re-import the project in IntelliJ.

#### Check Lombok Plugin:
- Go to: **File → Settings → Plugins**
- Search for "Lombok"
- Make sure it's **installed and enabled**
- Restart IntelliJ

#### Update Maven Compiler Plugin:
The pom.xml already has the correct configuration, but you can try:
- Right-click `pom.xml` → **Maven → Reload Project**

### 7. Alternative: Use Maven from Terminal
If IntelliJ continues to have issues, you can run from terminal:
```bash
cd /Users/ent00398/New1/lms-backend
mvn clean install
mvn spring-boot:run
```

## Common Causes:
1. **Java version mismatch** - IntelliJ using different Java than Maven
2. **Lombok annotation processor** not configured correctly
3. **Corrupted IntelliJ cache/index**
4. **Maven project not properly imported**

## Verify Java Version:
```bash
java -version  # Should show Java 21
mvn -version  # Should show Java 21
```

In IntelliJ: **Help → About** should show Java version used by IDE.



