# Student Bag Backend

A modern backend service for the Student Bag academic platform, built with Spring Boot and PostgreSQL. It powers authentication, academic management, schedules, tasks, notes, resources, notifications, analytics, and an AI-powered assistant for students and instructors.

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange" alt="Java 17" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/License-Academic-lightgrey" alt="License" />
</p>

## Overview

Student Bag Backend is a modular Spring Boot monolith that provides a rich set of REST APIs for university-related workflows. The system supports multiple user roles and includes features for:

- user authentication and role-based access
- academic structure and course management
- schedules, tasks, notes, and reminders
- resources, approvals, and notifications
- events and opportunity management
- analytics dashboards and reporting
- AI chatbot support with OpenAI integration

## Key Features

### 🔐 Authentication and Security
- registration for students, instructors, and administrators
- login and password change flows
- JWT-based authentication with stateless security
- protected endpoints for business logic and admin operations

### 🎓 Academic Management
- institutions, faculties, departments, terms, courses, course sections, and class sessions
- course synchronization support through a dedicated integration flow

### 📚 Productivity Tools
- tasks and subtasks
- notes and attachments
- resource library and personal folders
- schedule generation and preference handling

### 🔔 Notifications and Events
- recurring reminders for tasks and events
- weekly resource notifications
- Firebase-based push notification support

### 🤖 AI Assistant
- AI chat endpoints for academic help
- conversation history and message management
- OpenAI API integration for generating responses

## Tech Stack

- Java 17
- Spring Boot 3.5.x
- Spring Web / WebFlux
- Spring Data JPA / Hibernate
- Spring Security
- JWT (jjwt)
- PostgreSQL
- Flyway
- OpenAPI / Swagger (springdoc)
- Lombok
- Docker / Docker Compose
- Firebase Admin SDK
- OpenAI API via WebClient
- Tika and Tess4J

## Project Structure

```text
src/main/java/com/studentbag/backend/
├── administrator/
├── analytics/
├── auth/
├── chatbot/
├── common/
├── courses/
├── domain/
├── events/
├── grades/
├── institution/
├── instructor/
├── notes/
├── notifications/
├── resources/
├── schedule/
├── security/
├── student/
├── tasks/
├── users/
└── StudentBagBackendApplication.java
```

## Configuration

The main app configuration is in [src/main/resources/application.yaml](src/main/resources/application.yaml).

### Environment Variables

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/student_bag
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password

MAIL_USERNAME=your_mail_username
MAIL_PASSWORD=your_mail_password

JWT_SECRET=your_secret_key
JWT_EXPIRATION=86400000

OPENAI_API_KEY=your_openai_key
OPENAI_MODEL=gpt-4.1-mini
OPENAI_BASE_URL=https://api.openai.com/v1

GOOGLE_APPLICATION_CREDENTIALS=/app/src/main/resources/firebase/student-bag-fe020-firebase-adminsdk-fbsvc-973767165c.json
FIREBASE_STORAGE_BUCKET=your_bucket
```

## Getting Started

### Prerequisites
- Java 17 or later
- Maven
- PostgreSQL
- Docker (optional)

### Run Locally

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The backend will be available at:

```text
http://localhost:8080
```

### Run with Docker

```bash
docker compose up --build
```

This starts:
- PostgreSQL on port 5432
- the API service on port 8080

## API Overview

The API is exposed under the `/api` base path.

### Main Modules
- `/api/auth` for authentication and account management
- `/api/chatbot` for AI chat and conversation endpoints
- `/api/courses`, `/api/institutions`, `/api/faculties`, `/api/departments`, `/api/terms` for academic data
- `/api/tasks`, `/api/notes` for student productivity features
- `/api/events`, `/api/notifications` for event and notification flows
- `/api/resources` for resource management and approvals
- `/api/schedule` for schedule-related operations
- `/api/ritaj-sync` for course data synchronization

### Authentication

Most protected endpoints require a JWT bearer token:

```text
Authorization: Bearer <token>
```

## Security Notes

- JWT authentication is enforced through a custom filter
- session management is stateless
- CSRF is disabled in the current configuration
- sensitive values should never be committed to source control

## Deployment Notes

- PostgreSQL must be reachable before startup
- Docker Compose is the recommended local deployment method
- secrets should be injected through environment variables or a secure deployment environment

## Current Status

The backend includes a broad set of features for academic and student support, including:

- authentication and role-based access
- course and academic data management
- tasks, notes, schedules, and events
- notifications and Firebase push support
- AI chatbot integration with OpenAI
- scheduled background notifications

## Notes

This repository contains the backend side of the Student Bag platform. The architecture is a modular monolithic Spring Boot application rather than a microservices-based system.

## License

This project is developed for academic purposes.

