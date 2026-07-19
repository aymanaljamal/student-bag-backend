# ⚙️ Student Bag Backend

The server-side application for the Student Bag academic platform. Provides REST APIs for managing students, instructors, parents, courses, schedules, resources, notes, tasks, notifications, reports, and AI-powered academic support.

Built with **Spring Boot** and **PostgreSQL**, with secure authentication and role-based access control.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Requirements](#requirements)
  - [Environment Configuration](#environment-configuration)
  - [Database Configuration](#database-configuration)
  - [Running Locally](#running-locally)
  - [Running with Docker](#running-with-docker)
- [API Reference](#api-reference)
  - [Base URL](#api-base-url)
  - [Main Modules](#main-api-modules)
  - [Authentication](#authentication)
  - [User Roles](#user-roles)
- [Deployment Notes](#docker-deployment-notes)
- [Development Guidelines](#development-guidelines)
- [Git Workflow](#git-workflow)
- [Project Status](#project-status)
- [Related Project](#related-project)

---

## Overview

Student Bag is a smart academic assistance platform designed to support different university users from one system. The backend serves four user roles, each with specific permissions and access to different academic features:

| Role | Description |
|------|-------------|
| 🎓 **Student** | Tasks, notes, schedules, resources, events |
| 👨‍🏫 **Instructor** | Resource uploads, event/opportunity management |
| 🛠️ **Admin** | Users, courses, approvals, reports, notifications |
| 👪 **Parent** | Access to student academic information |

---

## Features

| Module | Includes |
|--------|----------|
| **Authentication & Authorization** | Registration/login, JWT auth, role-based access control, secure password handling, profile management |
| **Academic Management** | Courses, course sections, class sessions, student schedules, schedule generation, active schedule tracking |
| **Resources Management** | Upload/manage resources (files & links), instructor uploads, admin approval/rejection workflow, visibility control, history tracking |
| **Tasks & Notes** | Student tasks, subtasks, notes, attachments, local sync support |
| **Events & Opportunities** | Academic events, student registration, opportunities, cancellation, role-based access |
| **Notifications** | System notifications, admin announcements, task reminders, event updates, resource approval/rejection alerts |
| **Reports & Analytics** | Admin reports, course reports, user statistics, resource statistics, activity analytics |
| **AI Chatbot Support** | Academic assistant, study support, quiz generation, schedule help, task/course assistance |

---

## Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Java |
| Framework | Spring Boot |
| Security | Spring Security, JWT Authentication |
| Database | PostgreSQL |
| ORM | Spring Data JPA, Hibernate |
| API | REST API |
| Build Tool | Maven |
| Containerization | Docker |
| AI | OpenAI API |

---

## Project Structure

```text
student-bag-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/studentbag/
│   │   │       ├── auth/            # Authentication logic
│   │   │       ├── config/          # App configuration
│   │   │       ├── controller/      # REST controllers
│   │   │       ├── dto/             # Data transfer objects
│   │   │       ├── entity/          # JPA entities
│   │   │       ├── enums/           # Enum types
│   │   │       ├── exception/       # Exception handling
│   │   │       ├── repository/      # Database repositories
│   │   │       ├── security/        # Security configuration
│   │   │       ├── service/         # Business logic
│   │   │       └── StudentBagApplication.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │
│   └── test/
│
├── .mvn/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

---

## Getting Started

### Requirements

- Java 17 or later
- Maven
- PostgreSQL
- Docker and Docker Compose

### Environment Configuration

Create an environment file or configure the required variables inside `application.properties`:

```env
SERVER_PORT=8080

DB_URL=jdbc:postgresql://localhost:5432/student_bag
DB_USERNAME=postgres
DB_PASSWORD=your_password

JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000

OPENAI_API_KEY=your_openai_api_key
```

> ⚠️ **Never push real passwords, secret keys, or API keys to GitHub.**

### Database Configuration

Default PostgreSQL database name: `student_bag`

Example local configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/student_bag
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Running Locally

**1. Clone the repository**

```bash
git clone https://github.com/aymanajamal/student-bag-backend.git
cd student-bag-backend
```

**2. Install dependencies**

```bash
mvn clean install
```

**3. Run the application**

```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`.

### Running with Docker

**1. Build the image**

```bash
docker build -t student-bag-backend .
```

**2. Run with Docker Compose**

```bash
docker compose up -d
```

**3. Check running containers**

```bash
docker ps
```

**4. Stop containers**

```bash
docker compose down
```

---

## API Reference

### API Base URL

| Environment | URL |
|-------------|-----|
| Local development | `http://localhost:8080/api/v1` |
| Production (example) | `http://your-server-ip:8080/api/v1` |

### Main API Modules

```text
/api/v1/auth
/api/v1/users
/api/v1/courses
/api/v1/course-sections
/api/v1/class-sessions
/api/v1/schedules
/api/v1/resources
/api/v1/events
/api/v1/tasks
/api/v1/notes
/api/v1/notifications
/api/v1/reports
/api/v1/chatbot
```

### Authentication

Most endpoints require a JWT token. After login, include the token in the request header:

```text
Authorization: Bearer <your_token>
```

### User Roles

The system supports role-based access control. Available roles:

- `ADMIN`
- `INSTRUCTOR`
- `STUDENT`
- `PARENT`

Each role has access only to the endpoints and actions allowed by the backend security configuration.

---

## Docker Deployment Notes

When deploying the backend on a server:

- ✅ Make sure PostgreSQL is running
- ✅ Make sure port 8080 is open
- ✅ Make sure the database credentials are correct
- ✅ Keep secret keys inside environment variables
- ✅ Never expose private keys in GitHub
- ✅ Use Docker Compose for easier deployment

Useful commands:

```bash
docker compose pull
docker compose up -d
docker logs -f student-bag-api
docker restart student-bag-api
```

---

## Development Guidelines

- Keep controllers clean
- Put business logic inside services
- Use repositories only for database access
- Use DTOs instead of exposing entities directly
- Validate request data before saving it
- Keep API responses consistent
- Use meaningful exception messages
- Avoid hardcoded secrets
- Test endpoints before pushing changes

---

## Git Workflow

Recommended branch naming:

```text
feature/auth-module
feature/resources-management
feature/schedule-generator
feature/ai-chatbot
fix/docker-backend-setup
fix/notifications-service
fix/resources-approval
```

Basic commands:

```bash
git checkout -b feature/branch-name
git add .
git commit -m "Add clear commit message"
git push origin feature/branch-name
```

---

## Project Status

The backend is under active development. Current focus areas:

- Improving Docker deployment
- Completing resources approval workflow
- Enhancing notifications
- Improving reports and analytics
- Connecting AI chatbot features
- Stabilizing student schedule generation
- Improving API security and error handling

---

## Related Project

**Frontend repository:** [`student_bag_app`](https://github.com/aymanaljamal/student-bag-flutter) — built with Flutter, connects to this backend through REST APIs.

---

## Author

Developed by **Ayman Al Jamal**.

## License

This project is developed for academic purposes.
