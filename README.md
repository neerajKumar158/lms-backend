# LMS Backend - Learning Management System

A full-stack Learning Management System built with Spring Boot and Thymeleaf.

## Features

- **User Management**: Students, Teachers, and Organizations
- **Course Management**: Create, publish, and manage courses
- **Enrollment System**: Free and paid course enrollment
- **Payment Integration**: Razorpay payment gateway
- **Live Streaming**: Jitsi Meet integration for live sessions
- **Study Materials**: Upload and manage course materials
- **Progress Tracking**: Track student progress in courses

## Technology Stack

- Spring Boot 3.3.4
- Java 21
- Thymeleaf (Frontend)
- H2 Database (Development)
- JWT Authentication
- Razorpay Payment Gateway
- Jitsi Meet (Live Streaming)

## Quick Start

1. **Build the project:**
   ```bash
   mvn clean install
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Access the LMS:**
   - Open browser: `http://localhost:9192/ui/lms`
   - Register as Student, Teacher, or Organization
   - Start creating courses or enrolling!

## Configuration

### Payment Gateway
Configure Razorpay in `application.yaml`:
```yaml
razorpay:
  key-id: your_key_id
  key-secret: your_secret_key
  enabled: true
```

### File Upload
Files are stored in `uploads/` directory (configurable in `application.yaml`)

## API Documentation

See `LMS_QUICK_START.md` for detailed API documentation and usage examples.

## Project Structure

```
src/main/java/com/visionwaves/lms/
├── domain/          # Entity classes
├── repository/      # Data access layer
├── service/         # Business logic
├── web/            # Controllers
├── security/       # Security configuration
└── config/         # Configuration classes

src/main/resources/
├── templates/      # Thymeleaf templates
└── application.yaml # Configuration
```

## License

This project is part of VisionWaves LMS platform.



