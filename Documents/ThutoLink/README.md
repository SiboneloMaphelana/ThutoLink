# ThutoLink

ThutoLink is a multi-role school platform for administrators, teachers, parents, and learners. It combines role-based dashboards with class management, assignments, grading, attendance tracking, structured communication, notifications, audit logging, tenant isolation, and assignment/submission file handling.

This repository is split into:

- `frontend-thutoLink`: Angular 21 frontend with NgRx state management
- `backend-thutoLink`: Spring Boot 4 backend with JPA, Spring Security, and H2/PostgreSQL support

## What The App Does

ThutoLink supports four role-based experiences:

- `Admin`: school overview, class visibility, announcements, messages, and notification monitoring
- `Teacher`: publish assignments, upload assignment files, grade learner work, record attendance, post announcements, and message parents
- `Parent`: view linked learners, attendance issues, assignment progress, messages, and notifications
- `Learner`: view classes, download assignment files, submit work with text or file attachments, and review scores and feedback

## Key Features

- Role-based login and dashboard routing
- Demo accounts for all four user roles
- Tenant isolation by `schoolId` across domain data
- Centralized notification model with read/unread state
- Audit logging for grading, attendance, messaging, announcements, and submissions
- Rich seeded demo data with multiple classes, multiple children per parent, varied grades, and realistic timelines
- Assignment and submission file handling for PDF and image uploads
- Downloadable attachment support with stored metadata:
  - file name
  - content type
  - file size

## Tech Stack

- Frontend:
  - Angular 21
  - NgRx
  - Tailwind CSS 4
  - RxJS
- Backend:
  - Spring Boot 4
  - Spring Security
  - Spring Data JPA
  - H2
  - PostgreSQL driver

## Project Structure

```text
ThutoLink/
├── backend-thutoLink/
└── frontend-thutoLink/
```

## Getting Started

### 1. Start the backend

From `backend-thutoLink`:

```bash
./mvnw spring-boot:run
```

Backend defaults:

- API base URL: `http://localhost:8080/api`
- H2 console: `http://localhost:8080/h2-console`
- Default local DB: `jdbc:h2:file:./data/thutolink`

### 2. Start the frontend

From `frontend-thutoLink`:

```bash
npm install
npm start
```

Frontend defaults:

- App URL: `http://localhost:4200`

## Demo Accounts

Use any of the seeded demo users:

- `principal@thutolink.school` / `admin123`
- `teacher.nkosi@thutolink.school` / `teacher123`
- `parent.dlamini@thutolink.school` / `parent123`
- `amahle@thutolink.school` / `learner123`

## File Handling

Assignments and learner submissions can include optional attachments.

- Supported file types:
  - `application/pdf`
  - `image/jpeg`
  - `image/png`
  - `image/webp`
  - `image/gif`
- Maximum file size: `5 MB`
- Stored metadata:
  - `fileName`
  - `contentType`
  - `size`

Teachers can attach files when publishing assignments, and learners can attach files when submitting work. Attachments are also available for authenticated download through the backend.

## Notifications

Notifications are generated from backend domain events rather than directly from frontend screens.

Example triggers:

- assignment published
- assignment graded
- attendance marked absent or late
- announcement posted
- parent message sent

The notification feed tracks `read` and `readAt` state.

## Audit Logging

Important user actions are persisted in audit logs with:

- who performed the action
- what changed
- when it happened

Examples include:

- `GRADE_UPDATED`
- `ATTENDANCE_RECORDED`
- `ATTENDANCE_EDITED`
- `MESSAGE_SENT`
- `ANNOUNCEMENT_SENT`
- `ASSIGNMENT_PUBLISHED`
- submission create and resubmit events

## Multi-Tenant Data Isolation

Tenant isolation is enforced through `schoolId`.

- tenant-owned entities carry `schoolId`
- repository access is school-scoped
- service logic derives tenant context from the authenticated user
- request payloads do not choose tenant scope

## Testing

### Backend

```bash
cd backend-thutoLink
./mvnw test
```

Tests use in-memory H2 under `src/test/resources/application.properties`.

### Frontend

```bash
cd frontend-thutoLink
npm test
```

## Build Notes

The frontend production build may fail in offline environments because Angular attempts to inline Google Fonts from `fonts.googleapis.com` during the build.

## Seed Data Note

The backend demo seeder only runs when the database is empty. If you want to see fresh seeded data after schema or seed updates, clear the local H2 database files under:

```text
backend-thutoLink/data/
```

## Status

This README reflects the current implemented system, including:

- tenant isolation
- unified notifications
- audit logging
- richer demo data
- file uploads for assignments and submissions
