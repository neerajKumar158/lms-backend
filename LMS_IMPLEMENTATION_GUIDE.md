# LMS (Learning Management System) Implementation Guide

## Prerequisites

### 1. Software Requirements
- **Java 21** (already configured)
- **Maven 3.6+** (for dependency management)
- **IDE**: IntelliJ IDEA / Eclipse / VS Code
- **Database**: H2 (for development) or PostgreSQL/MySQL (for production)
- **Git** (for version control)

### 2. External Services (Optional but Recommended)
- **Payment Gateway**: Razorpay (already integrated) or Stripe/PayPal
- **File Storage**: AWS S3 / Google Cloud Storage / Local filesystem (for course materials)
- **Live Streaming**: 
  - **Option 1**: Jitsi Meet (free, open-source, embeddable)
  - **Option 2**: WebRTC with Spring WebSocket
  - **Option 3**: Third-party services (Zoom API, Twilio Video, Agora.io)

### 3. Additional Maven Dependencies Needed
- **File Upload**: Spring Boot Starter (already included)
- **WebSocket**: For real-time features (spring-boot-starter-websocket)
- **Email**: For notifications (spring-boot-starter-mail)
- **Scheduling**: For course schedules (spring-boot-starter-quartz or @Scheduled)

## Step-by-Step Implementation Plan

### Phase 1: Database Schema & Domain Models (Week 1)

#### 1.1 User Management
- Extend `UserAccount` to support:
  - User Type: STUDENT, TEACHER, ORGANIZATION
  - Profile information (bio, avatar, qualifications)
  - Verification status
  - Subscription/plan information

#### 1.2 Core LMS Entities
- **Course**: Title, description, price, category, instructor, status
- **CourseEnrollment**: Student enrollment in courses
- **Lecture**: Individual lessons within a course
- **StudyMaterial**: Files, videos, documents
- **LiveSession**: Scheduled live streaming sessions
- **Organization**: Company/institution details
- **Payment**: Course payment transactions
- **Assignment**: Student assignments
- **Quiz/Exam**: Assessment tools

### Phase 2: Authentication & Authorization (Week 1-2)

#### 2.1 Role-Based Access Control
- Implement role-based security:
  - `ROLE_STUDENT`: Can enroll, view courses, attend sessions
  - `ROLE_TEACHER`: Can create courses, upload materials, conduct live sessions
  - `ROLE_ORGANIZATION`: Can manage teachers, courses, students
  - `ROLE_ADMIN`: System administration

#### 2.2 Registration Flow
- Multi-step registration with role selection
- Email verification
- Profile completion

### Phase 3: Course Management (Week 2-3)

#### 3.1 Course CRUD Operations
- Create, Read, Update, Delete courses
- Course categories and tags
- Course search and filtering
- Course preview (free content)

#### 3.2 Study Materials
- File upload (PDF, DOCX, PPTX)
- Video upload/embedding (YouTube, Vimeo, or self-hosted)
- Audio files
- Material organization by lectures

### Phase 4: Enrollment & Payment (Week 3-4)

#### 4.1 Enrollment System
- Free course enrollment (instant)
- Paid course enrollment (after payment)
- Enrollment status tracking
- Progress tracking

#### 4.2 Payment Integration
- Integrate Razorpay (already available)
- Payment flow:
  1. Student selects course
  2. Redirect to payment page
  3. Process payment
  4. Enroll student on success
  5. Send confirmation email

### Phase 5: Live Streaming (Week 4-5)

#### 5.1 Live Session Management
- Schedule live sessions
- Session links and access control
- Recording capability (optional)

#### 5.2 Integration Options:

**Option A: Jitsi Meet (Recommended for Quick Start)**
- Embed Jitsi Meet in Thymeleaf template
- Generate unique room names per session
- Access control via JWT tokens

**Option B: WebRTC with Spring WebSocket**
- Custom WebRTC implementation
- More control but complex

**Option C: Third-party API**
- Zoom API, Twilio Video, Agora.io
- Requires API keys and subscriptions

### Phase 6: Organization Management (Week 5)

#### 6.1 Organization Features
- Organization registration
- Add teachers to organization
- Create courses under organization
- Student management
- Revenue sharing/reporting

### Phase 7: Frontend Development (Week 6-7)

#### 7.1 Thymeleaf Templates
- Homepage with course listings
- Course detail pages
- Student dashboard
- Teacher dashboard
- Organization dashboard
- Live session pages
- Payment pages

#### 7.2 Responsive Design
- Mobile-friendly UI
- Modern, clean design
- Bootstrap 5 or Tailwind CSS (via CDN)

### Phase 8: Additional Features (Week 7-8)

#### 8.1 Notifications
- Email notifications for:
  - Course enrollment
  - Live session reminders
  - Assignment deadlines
  - Payment confirmations

#### 8.2 Analytics & Reporting
- Course completion rates
- Student progress
- Revenue reports
- Popular courses

## Technical Architecture

### Database Schema Overview

```
UserAccount (extends existing)
├── userType: STUDENT | TEACHER | ORGANIZATION
├── profile fields
└── verification status

Course
├── title, description, price
├── instructor (Teacher)
├── organization (optional)
├── category
└── status: DRAFT | PUBLISHED | ARCHIVED

Lecture
├── course (Course)
├── title, description
├── order/sequence
└── materials (StudyMaterial[])

StudyMaterial
├── lecture (Lecture)
├── fileUrl
├── materialType: VIDEO | PDF | AUDIO | LINK
└── isFree: boolean

CourseEnrollment
├── student (UserAccount)
├── course (Course)
├── enrolledDate
├── progress
└── status: ACTIVE | COMPLETED | CANCELLED

LiveSession
├── course (Course)
├── instructor (Teacher)
├── scheduledDateTime
├── duration
├── meetingLink (Jitsi room URL)
└── status: SCHEDULED | ONGOING | COMPLETED

Organization
├── name, description
├── admin (UserAccount)
├── teachers (UserAccount[])
└── courses (Course[])

Payment
├── student (UserAccount)
├── course (Course)
├── amount
├── razorpayOrderId
├── razorpayPaymentId
└── status: PENDING | SUCCESS | FAILED
```

## Getting Started - Immediate Steps

1. **Update pom.xml** with additional dependencies
2. **Create domain entities** for LMS
3. **Create repositories** for data access
4. **Create services** for business logic
5. **Create controllers** for API endpoints
6. **Create Thymeleaf templates** for UI
7. **Configure file upload** settings
8. **Set up email** service (optional)
9. **Integrate live streaming** solution
10. **Test end-to-end** flows

## Next Steps

Let's start implementing! I'll create:
1. Updated domain models
2. Repository interfaces
3. Service classes
4. Controllers
5. Thymeleaf templates
6. Configuration files

Ready to begin? Let me know and I'll start with Phase 1!

