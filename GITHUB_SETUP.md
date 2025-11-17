# GitHub Setup Guide

This guide will help you push your LMS Backend code to GitHub.

## Step 1: Create a GitHub Account

1. Go to [https://github.com](https://github.com)
2. Click "Sign up" in the top right corner
3. Enter your email, create a password, and choose a username
4. Verify your email address

## Step 2: Create a New Repository on GitHub

1. After logging in, click the "+" icon in the top right corner
2. Select "New repository"
3. Fill in the repository details:
   - **Repository name**: `lms-backend` (or any name you prefer)
   - **Description**: "Learning Management System Backend - Spring Boot Application"
   - **Visibility**: Choose "Public" (free) or "Private" (requires paid plan)
   - **DO NOT** initialize with README, .gitignore, or license (we already have these)
4. Click "Create repository"

## Step 3: Push Your Code to GitHub

After creating the repository, GitHub will show you commands. Use these commands in your terminal:

### Option A: If you haven't set up SSH keys (use HTTPS)

```bash
cd /Users/ent00398/New1/lms-backend

# Add the remote repository (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/lms-backend.git

# Rename the branch to main (if needed)
git branch -M main

# Push the code to GitHub
git push -u origin main
```

You'll be prompted for your GitHub username and password (use a Personal Access Token, not your account password).

### Option B: If you have SSH keys set up

```bash
cd /Users/ent00398/New1/lms-backend

# Add the remote repository (replace YOUR_USERNAME with your GitHub username)
git remote add origin git@github.com:YOUR_USERNAME/lms-backend.git

# Rename the branch to main (if needed)
git branch -M main

# Push the code to GitHub
git push -u origin main
```

## Step 4: Create a Personal Access Token (if using HTTPS)

If you're using HTTPS and GitHub asks for a password:

1. Go to GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Give it a name like "LMS Backend Access"
4. Select scopes: Check "repo" (full control of private repositories)
5. Click "Generate token"
6. **Copy the token immediately** (you won't see it again)
7. Use this token as your password when pushing

## Quick Commands Summary

```bash
# Navigate to project directory
cd /Users/ent00398/New1/lms-backend

# Check current status
git status

# Add remote (replace YOUR_USERNAME and REPO_NAME)
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git

# Push to GitHub
git push -u origin main
```

## Future Updates

After the initial push, to update GitHub with new changes:

```bash
git add .
git commit -m "Description of your changes"
git push
```

## Repository URL Format

- HTTPS: `https://github.com/YOUR_USERNAME/REPO_NAME.git`
- SSH: `git@github.com:YOUR_USERNAME/REPO_NAME.git`

Replace:
- `YOUR_USERNAME` with your GitHub username
- `REPO_NAME` with your repository name (e.g., `lms-backend`)

