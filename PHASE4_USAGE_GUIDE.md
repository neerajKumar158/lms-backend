# Phase 4 Usage Guide - How to Use Production Hardening & Advanced Analytics

## Table of Contents
1. [Setting Up Profiles](#setting-up-profiles)
2. [Running the Application](#running-the-application)
3. [Using Advanced Analytics API](#using-advanced-analytics-api)
4. [Testing with Examples](#testing-with-examples)
5. [Frontend Integration](#frontend-integration)

---

## 1. Setting Up Profiles

### Development Mode (Default)
The application runs in development mode by default. No setup needed!

```bash
# Just run normally
mvn spring-boot:run
```

### Production Mode
Set environment variables before running:

**On Linux/Mac:**
```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_USERNAME=lms_user
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your_base64_encoded_secret_key_here
export APP_BASE_URL=https://your-domain.com
export EMAIL_NOTIFICATIONS_ENABLED=true
export MAIL_USERNAME=your_email@gmail.com
export MAIL_PASSWORD=your_app_password

mvn spring-boot:run
```

**On Windows (PowerShell):**
```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_USERNAME="lms_user"
$env:DB_PASSWORD="your_secure_password"
$env:JWT_SECRET="your_base64_encoded_secret_key_here"
# ... other variables

mvn spring-boot:run
```

**Using .env file (recommended):**
Create a `.env` file in the project root:
```env
SPRING_PROFILES_ACTIVE=prod
DB_USERNAME=lms_user
DB_PASSWORD=your_secure_password
JWT_SECRET=your_base64_encoded_secret_key_here
APP_BASE_URL=https://your-domain.com
EMAIL_NOTIFICATIONS_ENABLED=true
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

Then load it before running (use a tool like `dotenv-cli` or source it manually).

---

## 2. Running the Application

### Check Active Profile
After starting, check logs to see which profile is active:
```
The following profiles are active: dev
```
or
```
The following profiles are active: prod
```

### Verify Configuration
- **Dev mode**: SQL queries will be logged to console
- **Prod mode**: SQL logging disabled, errors logged to file

---

## 3. Using Advanced Analytics API

### Step 1: Get Authentication Token

First, login to get a JWT token:

```bash
# Login as Organization Admin
curl -X POST http://localhost:9192/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "org@example.com",
    "password": "password123"
  }'

# Response:
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "userType": "ORGANIZATION",
#   ...
# }
```

Save the token for next requests.

### Step 2: Get Organization Analytics

```bash
# Replace {organizationId} with your organization ID
# Replace {token} with the token from Step 1

curl -X GET http://localhost:9192/api/lms/analytics/advanced/organization/1 \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json"
```

**Response includes:**
```json
{
  "organizationId": 1,
  "organizationName": "My Organization",
  "totalCourses": 10,
  "totalTeachers": 5,
  "revenue": {
    "total": 50000.00,
    "monthly": 5000.00,
    "yearly": 45000.00,
    "totalTransactions": 150,
    "averageTransactionValue": 333.33
  },
  "enrollments": {
    "total": 200,
    "active": 150,
    "completed": 50,
    "uniqueStudents": 120,
    "completionRate": 25.0
  },
  "teacherPerformance": [...],
  "coursePerformance": [...],
  "studentAnalytics": {...},
  "trends": {
    "enrollmentTrend": {
      "2024-01": 20,
      "2024-02": 25,
      ...
    },
    "revenueTrend": {
      "2024-01": 5000.00,
      "2024-02": 6000.00,
      ...
    }
  },
  "generatedAt": "2024-03-15T10:30:00"
}
```

### Step 3: Get Teacher Analytics

```bash
# Replace {teacherId} with teacher's user ID
curl -X GET http://localhost:9192/api/lms/analytics/advanced/teacher/2 \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json"
```

**Response includes:**
```json
{
  "teacherId": 2,
  "teacherName": "John Doe",
  "email": "john@example.com",
  "totalCourses": 5,
  "revenue": {...},
  "enrollments": {...},
  "coursePerformance": [...],
  "studentProgress": {
    "totalEnrollments": 100,
    "averageProgress": 65.5,
    "progressDistribution": {
      "0-25%": 10,
      "26-50%": 20,
      "51-75%": 30,
      "76-100%": 40
    }
  },
  "assessmentAnalytics": {
    "totalAssignments": 20,
    "totalSubmissions": 150,
    "submissionRate": 7.5,
    "averageAssignmentScore": 85.5,
    "totalQuizzes": 10,
    "totalQuizAttempts": 200,
    "averageQuizScore": 78.5
  },
  "trends": {...}
}
```

### Step 4: Export Data (CSV-ready)

```bash
# Export enrollments
curl -X GET http://localhost:9192/api/lms/analytics/advanced/organization/1/export/enrollments \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json"

# Export revenue
curl -X GET http://localhost:9192/api/lms/analytics/advanced/organization/1/export/revenue \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json"
```

**Enrollments Export Response:**
```json
{
  "organizationName": "My Organization",
  "exportType": "enrollments",
  "data": [
    {
      "courseTitle": "Introduction to Java",
      "studentName": "Alice Smith",
      "studentEmail": "alice@example.com",
      "enrolledDate": "2024-01-15T10:00:00",
      "status": "ACTIVE",
      "progress": "65%"
    },
    ...
  ],
  "generatedAt": "2024-03-15T10:30:00"
}
```

---

## 4. Testing with Examples

### Example 1: JavaScript/Node.js

```javascript
const axios = require('axios');

const BASE_URL = 'http://localhost:9192';
let token = '';

// Step 1: Login
async function login(email, password) {
  const response = await axios.post(`${BASE_URL}/api/auth/login`, {
    email,
    password
  });
  token = response.data.token;
  console.log('Logged in! Token:', token.substring(0, 20) + '...');
  return token;
}

// Step 2: Get Organization Analytics
async function getOrganizationAnalytics(orgId) {
  try {
    const response = await axios.get(
      `${BASE_URL}/api/lms/analytics/advanced/organization/${orgId}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error:', error.response?.data || error.message);
    throw error;
  }
}

// Step 3: Get Teacher Analytics
async function getTeacherAnalytics(teacherId) {
  try {
    const response = await axios.get(
      `${BASE_URL}/api/lms/analytics/advanced/teacher/${teacherId}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error:', error.response?.data || error.message);
    throw error;
  }
}

// Usage
(async () => {
  await login('org@example.com', 'password123');
  
  const orgAnalytics = await getOrganizationAnalytics(1);
  console.log('Organization Revenue:', orgAnalytics.revenue);
  console.log('Total Enrollments:', orgAnalytics.enrollments.total);
  
  const teacherAnalytics = await getTeacherAnalytics(2);
  console.log('Teacher Courses:', teacherAnalytics.totalCourses);
  console.log('Average Progress:', teacherAnalytics.studentProgress.averageProgress);
})();
```

### Example 2: Python

```python
import requests

BASE_URL = 'http://localhost:9192'
token = ''

# Step 1: Login
def login(email, password):
    global token
    response = requests.post(f'{BASE_URL}/api/auth/login', json={
        'email': email,
        'password': password
    })
    token = response.json()['token']
    print(f'Logged in! Token: {token[:20]}...')
    return token

# Step 2: Get Organization Analytics
def get_organization_analytics(org_id):
    headers = {
        'Authorization': f'Bearer {token}',
        'Content-Type': 'application/json'
    }
    response = requests.get(
        f'{BASE_URL}/api/lms/analytics/advanced/organization/{org_id}',
        headers=headers
    )
    response.raise_for_status()
    return response.json()

# Step 3: Get Teacher Analytics
def get_teacher_analytics(teacher_id):
    headers = {
        'Authorization': f'Bearer {token}',
        'Content-Type': 'application/json'
    }
    response = requests.get(
        f'{BASE_URL}/api/lms/analytics/advanced/teacher/{teacher_id}',
        headers=headers
    )
    response.raise_for_status()
    return response.json()

# Usage
if __name__ == '__main__':
    login('org@example.com', 'password123')
    
    org_data = get_organization_analytics(1)
    print(f"Total Revenue: ₹{org_data['revenue']['total']}")
    print(f"Total Enrollments: {org_data['enrollments']['total']}")
    
    teacher_data = get_teacher_analytics(2)
    print(f"Teacher Courses: {teacher_data['totalCourses']}")
    print(f"Avg Progress: {teacher_data['studentProgress']['averageProgress']}%")
```

### Example 3: Postman Collection

Create a Postman collection with these requests:

1. **Login**
   - Method: POST
   - URL: `http://localhost:9192/api/auth/login`
   - Body (JSON):
     ```json
     {
       "email": "org@example.com",
       "password": "password123"
     }
     ```
   - Tests: Save token to environment variable
     ```javascript
     pm.environment.set("token", pm.response.json().token);
     ```

2. **Get Organization Analytics**
   - Method: GET
   - URL: `http://localhost:9192/api/lms/analytics/advanced/organization/1`
   - Headers:
     - `Authorization: Bearer {{token}}`
     - `Content-Type: application/json`

3. **Get Teacher Analytics**
   - Method: GET
   - URL: `http://localhost:9192/api/lms/analytics/advanced/teacher/2`
   - Headers:
     - `Authorization: Bearer {{token}}`
     - `Content-Type: application/json`

---

## 5. Frontend Integration

### React Example

```jsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';

function OrganizationAnalytics({ organizationId }) {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(
          `/api/lms/analytics/advanced/organization/${organizationId}`,
          {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            }
          }
        );
        setAnalytics(response.data);
      } catch (err) {
        setError(err.response?.data?.error || 'Failed to load analytics');
      } finally {
        setLoading(false);
      }
    };

    fetchAnalytics();
  }, [organizationId]);

  if (loading) return <div>Loading analytics...</div>;
  if (error) return <div>Error: {error}</div>;
  if (!analytics) return null;

  return (
    <div className="analytics-dashboard">
      <h2>{analytics.organizationName} Analytics</h2>
      
      <div className="stats-grid">
        <div className="stat-card">
          <h3>Total Revenue</h3>
          <p>₹{analytics.revenue.total.toLocaleString()}</p>
          <small>Monthly: ₹{analytics.revenue.monthly.toLocaleString()}</small>
        </div>
        
        <div className="stat-card">
          <h3>Total Enrollments</h3>
          <p>{analytics.enrollments.total}</p>
          <small>Active: {analytics.enrollments.active}</small>
        </div>
        
        <div className="stat-card">
          <h3>Completion Rate</h3>
          <p>{analytics.enrollments.completionRate.toFixed(1)}%</p>
        </div>
        
        <div className="stat-card">
          <h3>Unique Students</h3>
          <p>{analytics.enrollments.uniqueStudents}</p>
        </div>
      </div>

      {/* Teacher Performance Table */}
      <div className="teacher-performance">
        <h3>Teacher Performance</h3>
        <table>
          <thead>
            <tr>
              <th>Teacher</th>
              <th>Courses</th>
              <th>Enrollments</th>
              <th>Revenue</th>
              <th>Rating</th>
            </tr>
          </thead>
          <tbody>
            {analytics.teacherPerformance.map(teacher => (
              <tr key={teacher.teacherId}>
                <td>{teacher.teacherName}</td>
                <td>{teacher.totalCourses}</td>
                <td>{teacher.totalEnrollments}</td>
                <td>₹{teacher.totalRevenue.toLocaleString()}</td>
                <td>{teacher.averageRating.toFixed(1)} ⭐</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Trends Chart (using Chart.js or similar) */}
      <div className="trends-chart">
        <h3>Enrollment Trends (Last 6 Months)</h3>
        {/* Render chart using analytics.trends.enrollmentTrend */}
      </div>
    </div>
  );
}

export default OrganizationAnalytics;
```

### HTML/JavaScript Example

```html
<!DOCTYPE html>
<html>
<head>
    <title>Organization Analytics</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <h1>Organization Analytics Dashboard</h1>
    <div id="analytics-container"></div>

    <script>
        const token = localStorage.getItem('token');
        const organizationId = 1; // Get from URL or context

        async function loadAnalytics() {
            try {
                const response = await fetch(
                    `/api/lms/analytics/advanced/organization/${organizationId}`,
                    {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json'
                        }
                    }
                );

                if (!response.ok) {
                    throw new Error('Failed to load analytics');
                }

                const data = await response.json();
                displayAnalytics(data);
            } catch (error) {
                console.error('Error:', error);
                document.getElementById('analytics-container').innerHTML = 
                    '<p>Error loading analytics: ' + error.message + '</p>';
            }
        }

        function displayAnalytics(data) {
            const container = document.getElementById('analytics-container');
            
            container.innerHTML = `
                <div class="stats">
                    <div>
                        <h3>Total Revenue</h3>
                        <p>₹${data.revenue.total.toLocaleString()}</p>
                    </div>
                    <div>
                        <h3>Total Enrollments</h3>
                        <p>${data.enrollments.total}</p>
                    </div>
                    <div>
                        <h3>Completion Rate</h3>
                        <p>${data.enrollments.completionRate.toFixed(1)}%</p>
                    </div>
                </div>
                
                <canvas id="trendsChart"></canvas>
            `;

            // Render trends chart
            const ctx = document.getElementById('trendsChart').getContext('2d');
            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: Object.keys(data.trends.enrollmentTrend),
                    datasets: [{
                        label: 'Enrollments',
                        data: Object.values(data.trends.enrollmentTrend),
                        borderColor: 'rgb(75, 192, 192)',
                        tension: 0.1
                    }]
                }
            });
        }

        // Load analytics on page load
        loadAnalytics();
    </script>
</body>
</html>
```

---

## 6. Common Issues & Solutions

### Issue: "Access denied" (403)
**Solution**: Make sure you're logged in as:
- Organization admin (for organization analytics)
- Teacher (for own analytics) or Organization admin/Admin (for any teacher)
- Admin (for any analytics)

### Issue: "Organization not found" (404)
**Solution**: Verify the organization ID exists. Check your user's organization:
```bash
# Get your user info
curl -X GET http://localhost:9192/api/profile \
  -H "Authorization: Bearer {token}"
```

### Issue: "Invalid authorization header"
**Solution**: Make sure the token is prefixed with "Bearer ":
```javascript
headers: {
  'Authorization': 'Bearer ' + token  // Note the space after "Bearer"
}
```

### Issue: Analytics show zero values
**Solution**: 
- Make sure you have courses, enrollments, and payments in the database
- Check that the organization/teacher IDs are correct
- Verify data exists for the time period being analyzed

---

## 7. Next Steps

1. **Create Frontend Pages**: Build UI pages to visualize the analytics data
2. **Add CSV Export**: Convert export data to downloadable CSV files
3. **Add PDF Reports**: Generate PDF reports from analytics data
4. **Add Filters**: Allow filtering analytics by date range, course, etc.
5. **Add Charts**: Visualize trends using Chart.js, D3.js, or similar

---

## Quick Reference

| Endpoint | Method | Access | Description |
|----------|--------|--------|-------------|
| `/api/lms/analytics/advanced/organization/{id}` | GET | Org Admin, Admin | Get organization analytics |
| `/api/lms/analytics/advanced/teacher/{id}` | GET | Teacher (own), Org Admin, Admin | Get teacher analytics |
| `/api/lms/analytics/advanced/organization/{id}/export/enrollments` | GET | Org Admin, Admin | Export enrollments data |
| `/api/lms/analytics/advanced/organization/{id}/export/revenue` | GET | Org Admin, Admin | Export revenue data |

**Note**: All endpoints require `Authorization: Bearer {token}` header.

