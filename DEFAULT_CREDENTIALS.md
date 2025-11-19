# Default User Credentials

The application automatically creates two default users on startup. These users are created in the database and persist across application restarts.

## Default Users

### Teacher Account
- **Email:** `teacher@lms.com`
- **Password:** `teacher123`
- **Role:** TEACHER
- **Name:** Default Teacher
- **Status:** Email verified, Profile completed

### Student Account
- **Email:** `student@lms.com`
- **Password:** `student123`
- **Role:** STUDENT
- **Name:** Default Student
- **Status:** Email verified, Profile completed

## Important Notes

1. **These users are created automatically** when the application starts for the first time
2. **Users persist in the database** - they will not be removed when you restart the application
3. **Change passwords in production** - These are default credentials for development/testing only
4. **If users already exist**, the application will skip creating them (no duplicates)

## Login Instructions

1. Navigate to: `http://localhost:9192/ui/auth`
2. Enter one of the email addresses above
3. Enter the corresponding password
4. Click "Login"

## Access

- **Teacher account** can access:
  - Teacher Dashboard (`/ui/lms/teacher/dashboard`)
  - Create and manage courses
  - Create assignments and quizzes
  - Schedule live sessions
  - View student report cards

- **Student account** can access:
  - Student Dashboard (`/ui/lms/student/dashboard`)
  - Browse and enroll in courses
  - Submit assignments
  - Take quizzes
  - View own report cards
  - Attend live sessions





