# LMS Quick Start Guide

## What Has Been Implemented

### ✅ Core Features

1. **User Management**
   - Registration with user types: STUDENT, TEACHER, ORGANIZATION
   - JWT-based authentication
   - Role-based access control

2. **Course Management**
   - Create, read, update courses
   - Course categories
   - Course publishing workflow (DRAFT → PUBLISHED)
   - Free and paid courses

3. **Lecture & Study Materials**
   - Add lectures to courses
   - Upload study materials (PDF, Video, Audio, Documents)
   - Organize materials by lectures

4. **Enrollment System**
   - Student enrollment in courses
   - Progress tracking
   - Free course instant enrollment
   - Paid course enrollment after payment

5. **Payment Integration**
   - Razorpay integration for course payments
   - Payment verification
   - Automatic enrollment after successful payment

6. **Live Streaming**
   - Jitsi Meet integration
   - Schedule live sessions
   - Join live sessions via embedded player
   - Session management (start/end)

7. **File Upload**
   - Study material uploads
   - File storage configuration
   - File serving via static resources

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register (with userType: STUDENT, TEACHER, ORGANIZATION)
- `POST /api/auth/login` - Login

### Courses
- `GET /api/lms/courses` - Get all published courses
- `GET /api/lms/courses/free` - Get free courses
- `GET /api/lms/courses/search?keyword=...` - Search courses
- `GET /api/lms/courses/{id}` - Get course details
- `POST /api/lms/courses` - Create course (Teacher only)
- `PUT /api/lms/courses/{id}` - Update course (Teacher only)
- `POST /api/lms/courses/{id}/publish` - Publish course (Teacher only)
- `GET /api/lms/courses/{id}/lectures` - Get course lectures
- `POST /api/lms/courses/{id}/lectures` - Add lecture (Teacher only)

### Enrollments
- `GET /api/lms/enrollments` - Get my enrollments
- `POST /api/lms/enrollments/course/{courseId}` - Enroll in course
- `GET /api/lms/enrollments/course/{courseId}` - Check enrollment status
- `PUT /api/lms/enrollments/{enrollmentId}/progress` - Update progress

### Payments
- `POST /api/lms/payments/course/{courseId}/create-order` - Create payment order
- `POST /api/lms/payments/verify` - Verify payment

### Live Sessions
- `GET /api/lms/live-sessions` - Get upcoming sessions
- `GET /api/lms/live-sessions/instructor` - Get my sessions (Teacher)
- `POST /api/lms/live-sessions` - Create session (Teacher)
- `POST /api/lms/live-sessions/{id}/start` - Start session (Teacher)
- `POST /api/lms/live-sessions/{id}/end` - End session (Teacher)

### File Upload
- `POST /api/lms/upload/material/{lectureId}` - Upload study material

## UI Pages

- `/ui/lms` - LMS Homepage
- `/ui/lms/courses` - Browse all courses
- `/ui/lms/course/{id}` - Course detail page
- `/ui/lms/student/dashboard` - Student dashboard
- `/ui/lms/teacher/dashboard` - Teacher dashboard
- `/ui/lms/organization/dashboard` - Organization dashboard
- `/ui/lms/live/{sessionId}` - Live session page

## How to Start

1. **Build the project:**
   ```bash
   mvn clean install
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Access the LMS:**
   - Open browser: `http://localhost:9191/ui/lms`
   - Register as Student, Teacher, or Organization
   - Start creating courses or enrolling!

## Configuration

### File Upload
Files are stored in `uploads/` directory (configurable in `application.yaml`)

### Payment Gateway
Configure Razorpay credentials in `application.yaml`:
```yaml
razorpay:
  key-id: your_key_id
  key-secret: your_secret_key
  enabled: true
```

### Database
Currently using H2 in-memory database. For production, switch to PostgreSQL/MySQL:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lms
    username: your_username
    password: your_password
```

## Next Steps

1. **Add more features:**
   - Assignments and quizzes
   - Certificates
   - Discussion forums
   - Notifications (email)
   - Analytics dashboard

2. **Production readiness:**
   - Switch to PostgreSQL/MySQL
   - Configure proper file storage (S3, etc.)
   - Set up email service
   - Add proper error handling
   - Implement caching
   - Add rate limiting

3. **Enhancements:**
   - Video streaming (self-hosted or Vimeo/YouTube)
   - Mobile app support
   - Advanced search and filters
   - Course recommendations
   - Social features

## Testing

1. **Register as Teacher:**
   ```json
   POST /api/auth/register
   {
     "email": "teacher@example.com",
     "password": "password123",
     "userType": "TEACHER",
     "name": "John Teacher"
   }
   ```

2. **Create a Course:**
   ```json
   POST /api/lms/courses
   {
     "title": "Introduction to Java",
     "description": "Learn Java programming",
     "price": "999.00",
     "level": "BEGINNER"
   }
   ```

3. **Register as Student:**
   ```json
   POST /api/auth/register
   {
     "email": "student@example.com",
     "password": "password123",
     "userType": "STUDENT",
     "name": "Jane Student"
   }
   ```

4. **Enroll in Course:**
   - For free courses: `POST /api/lms/enrollments/course/{courseId}`
   - For paid courses: Create payment order first, then verify payment

## Support

For issues or questions, refer to `LMS_IMPLEMENTATION_GUIDE.md` for detailed architecture and implementation details.

