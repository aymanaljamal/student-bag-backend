<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=12&height=220&section=header&text=Student%20Bag%20Backend&fontSize=52&fontColor=fff&animation=fadeIn&fontAlignY=38&desc=Powering%20the%20Student%20Bag%20Academic%20Platform&descAlignY=58&descSize=18"/>

<br/>

<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=600&size=22&pause=1000&color=6DB33F&center=true&vCenter=true&width=650&lines=Spring+Boot+%2B+PostgreSQL;JWT+Auth+%7C+Academic+APIs+%7C+AI+Assistant;Built+for+the+Student+Bag+Ecosystem+%F0%9F%8E%93" alt="Typing SVG" />

<br/><br/>

<img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17" />
<img src="https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot" />
<img src="https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
<img src="https://img.shields.io/badge/License-Academic-lightgrey?style=for-the-badge" alt="License" />

</div>

A modern backend service for the **Student Bag** academic platform, built with **Spring Boot** and **PostgreSQL**. It powers authentication, academic management, schedules, tasks, notes, resources, notifications, analytics, and an AI-powered assistant for students and instructors.

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Configuration](#-configuration)
- [Getting Started](#-getting-started)
- [API Overview](#-api-overview)
- [Security Notes](#-security-notes)
- [Deployment Notes](#-deployment-notes)
- [Current Status](#-current-status)

---

## 🔎 Overview

Student Bag Backend is a modular Spring Boot monolith that provides a rich set of REST APIs for university-related workflows. The system supports multiple user roles and includes features for:

- 🔐 User authentication and role-based access
- 🎓 Academic structure and course management
- 📅 Schedules, tasks, notes, and reminders
- 📚 Resources, approvals, and notifications
- 🎉 Events and opportunity management
- 📊 Analytics dashboards and reporting
- 🤖 AI chatbot support with OpenAI integration

---

## ✨ Key Features

### 🔐 Authentication and Security
- Registration for students, instructors, and administrators
- Login and password change flows
- JWT-based authentication with stateless security
- Protected endpoints for business logic and admin operations

### 🎓 Academic Management
- Institutions, faculties, departments, terms, courses, course sections, and class sessions
- Course synchronization support through a dedicated integration flow

### 📚 Productivity Tools
- Tasks and subtasks
- Notes and attachments
- Resource library and personal folders
- Schedule generation and preference handling

### 🔔 Notifications and Events
- Recurring reminders for tasks and events
- Weekly resource notifications
- Firebase-based push notification support

### 🤖 AI Assistant
- AI chat endpoints for academic help
- Conversation history and message management
- OpenAI API integration for generating responses

---

## 🛠️ Tech Stack

<div align="center">
<img src="https://skillicons.dev/icons?i=java,spring,postgres,docker,swagger&theme=dark" />
</div>

<br/>

| Category | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.x |
| Web | Spring Web / WebFlux |
| Data | Spring Data JPA / Hibernate |
| Security | Spring Security, JWT (jjwt) |
| Database | PostgreSQL |
| Migrations | Flyway |
| API Docs | OpenAPI / Swagger (springdoc) |
| Boilerplate | Lombok |
| Deployment | Docker / Docker Compose |
| Push Notifications | Firebase Admin SDK |
| AI | OpenAI API via WebClient |
| File Processing | Tika, Tess4J |

---

## 📂 Project Structure

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

---

## ⚙️ Configuration

The main app configuration is in [`src/main/resources/application.yaml`](src/main/resources/application.yaml).

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

---

## 🚀 Getting Started

### Prerequisites
- ☕ Java 17 or later
- 📦 Maven
- 🐘 PostgreSQL
- 🐳 Docker (optional)

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
- PostgreSQL on port `5432`
- The API service on port `8080`

---

## 📡 API Overview

The API is exposed under the `/api` base path.

### Main Modules

| Endpoint | Purpose |
|---|---|
| `/api/auth` | Authentication and account management |
| `/api/chatbot` | AI chat and conversation endpoints |
| `/api/courses`, `/api/institutions`, `/api/faculties`, `/api/departments`, `/api/terms` | Academic data |
| `/api/tasks`, `/api/notes` | Student productivity features |
| `/api/events`, `/api/notifications` | Event and notification flows |
| `/api/resources` | Resource management and approvals |
| `/api/schedule` | Schedule-related operations |
| `/api/ritaj-sync` | Course data synchronization |

### Authentication

Most protected endpoints require a JWT bearer token:

```text
Authorization: Bearer <token>
```

---

## 🔒 Security Notes

- JWT authentication is enforced through a custom filter
- Session management is stateless
- CSRF is disabled in the current configuration
- Sensitive values should never be committed to source control

---

## 🚢 Deployment Notes

- PostgreSQL must be reachable before startup
- Docker Compose is the recommended local deployment method
- Secrets should be injected through environment variables or a secure deployment environment

---

## 📈 Current Status

The backend includes a broad set of features for academic and student support, including:

- ✅ Authentication and role-based access
- ✅ Course and academic data management
- ✅ Tasks, notes, schedules, and events
- ✅ Notifications and Firebase push support
- ✅ AI chatbot integration with OpenAI
- ✅ Scheduled background notifications

> This repository contains the backend side of the Student Bag platform. The architecture is a modular monolithic Spring Boot application rather than a microservices-based system.

---

<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=12&height=150&section=footer"/>

**Made with 💛 by [Ayman Aljamal](https://github.com/aymanaljamal)**

*Developed for academic purposes.*

</div>
