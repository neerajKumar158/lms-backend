#!/bin/bash

# Test script for Advanced Analytics API
# Usage: ./test-analytics.sh

BASE_URL="http://localhost:9192"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== LMS Advanced Analytics API Test ===${NC}\n"

# Step 1: Login
echo "Step 1: Logging in..."
echo "Enter your email: "
read EMAIL
echo "Enter your password: "
read -s PASSWORD

LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo -e "${RED}Login failed!${NC}"
  echo "Response: $LOGIN_RESPONSE"
  exit 1
fi

echo -e "${GREEN}✓ Login successful!${NC}\n"

# Step 2: Get user info to determine organization/teacher ID
echo "Step 2: Getting user information..."
USER_RESPONSE=$(curl -s -X GET "$BASE_URL/api/profile" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json")

echo "User Info: $USER_RESPONSE"
echo ""

# Step 3: Test Organization Analytics (if user is organization admin)
echo "Step 3: Testing Organization Analytics..."
echo "Enter Organization ID (or press Enter to skip): "
read ORG_ID

if [ ! -z "$ORG_ID" ]; then
  echo "Fetching organization analytics..."
  ORG_ANALYTICS=$(curl -s -X GET "$BASE_URL/api/lms/analytics/advanced/organization/$ORG_ID" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json")
  
  if echo "$ORG_ANALYTICS" | grep -q "error"; then
    echo -e "${RED}✗ Error: $ORG_ANALYTICS${NC}"
  else
    echo -e "${GREEN}✓ Organization Analytics retrieved!${NC}"
    echo "$ORG_ANALYTICS" | python3 -m json.tool 2>/dev/null || echo "$ORG_ANALYTICS"
  fi
  echo ""
fi

# Step 4: Test Teacher Analytics
echo "Step 4: Testing Teacher Analytics..."
echo "Enter Teacher ID (or press Enter to skip): "
read TEACHER_ID

if [ ! -z "$TEACHER_ID" ]; then
  echo "Fetching teacher analytics..."
  TEACHER_ANALYTICS=$(curl -s -X GET "$BASE_URL/api/lms/analytics/advanced/teacher/$TEACHER_ID" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json")
  
  if echo "$TEACHER_ANALYTICS" | grep -q "error"; then
    echo -e "${RED}✗ Error: $TEACHER_ANALYTICS${NC}"
  else
    echo -e "${GREEN}✓ Teacher Analytics retrieved!${NC}"
    echo "$TEACHER_ANALYTICS" | python3 -m json.tool 2>/dev/null || echo "$TEACHER_ANALYTICS"
  fi
  echo ""
fi

# Step 5: Test Export Data
if [ ! -z "$ORG_ID" ]; then
  echo "Step 5: Testing Export Data..."
  echo "Testing enrollments export..."
  EXPORT_ENROLLMENTS=$(curl -s -X GET "$BASE_URL/api/lms/analytics/advanced/organization/$ORG_ID/export/enrollments" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json")
  
  if echo "$EXPORT_ENROLLMENTS" | grep -q "error"; then
    echo -e "${RED}✗ Error: $EXPORT_ENROLLMENTS${NC}"
  else
    echo -e "${GREEN}✓ Enrollments export retrieved!${NC}"
    echo "Data rows: $(echo "$EXPORT_ENROLLMENTS" | grep -o '"data":\[.*\]' | grep -o '{' | wc -l)"
  fi
  
  echo ""
  echo "Testing revenue export..."
  EXPORT_REVENUE=$(curl -s -X GET "$BASE_URL/api/lms/analytics/advanced/organization/$ORG_ID/export/revenue" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json")
  
  if echo "$EXPORT_REVENUE" | grep -q "error"; then
    echo -e "${RED}✗ Error: $EXPORT_REVENUE${NC}"
  else
    echo -e "${GREEN}✓ Revenue export retrieved!${NC}"
    echo "Data rows: $(echo "$EXPORT_REVENUE" | grep -o '"data":\[.*\]' | grep -o '{' | wc -l)"
  fi
fi

echo ""
echo -e "${GREEN}=== Test Complete ===${NC}"

