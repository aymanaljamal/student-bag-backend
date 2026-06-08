# Student Bag Backend

Student Bag Backend is the server-side application for the Student Bag academic platform.  
It provides REST APIs for managing students, instructors, parents, courses, schedules, resources, notes, tasks, notifications, reports, and AI-powered academic support.

The backend is built using Spring Boot and PostgreSQL, with secure authentication and role-based access control.

---

## Overview

Student Bag is a smart academic assistance platform designed to support different university users from one system.

The backend serves the following user roles:

- Student
- Instructor
- Admin
- Parent

Each role has specific permissions and access to different academic features.

---

## Main Features

### Authentication and Authorization

- User registration and login
- JWT-based authentication
- Role-based access control
- Secure password handling
- User profile management

### Academic Management

- Courses management
- Course sections
- Class sessions
- Student schedules
- Schedule generation
- Active schedule tracking

### Resources Management

- Upload and manage academic resources
- Support files and external links
- Instructor uploads
- Admin approval and rejection workflow
- Resource visibility control
- Resource history tracking

### Tasks and Notes

- Student tasks
- Subtasks
- Notes
- Attachments
- Local synchronization support

### Events and Opportunities

- Academic events
- Student registration
- Opportunities management
- Event cancellation
- Role-based event access

### Notifications

- System notifications
- Admin announcements
- Task reminders
- Event updates
- Resource approval or rejection notifications

### Reports and Analytics

- Admin reports
- Course reports
- User statistics
- Resource statistics
- Academic activity analytics

### AI Chatbot Support

- AI-powered academic assistant
- Study support
- Quiz generation
- Schedule help
- Task and course assistance

---

## Technologies Used

- Java
- Spring Boot
- Spring Security
- JWT Authentication
- PostgreSQL
- Spring Data JPA
- Hibernate
- REST API
- Docker
- Maven
- OpenAI API

---

## Project Structure

```text
student-bag-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── studentbag/
│   │   │           ├── auth/
│   │   │           ├── config/
│   │   │           ├── controller/
│   │   │           ├── dto/
│   │   │           ├── entity/
│   │   │           ├── enums/
│   │   │           ├── exception/
│   │   │           ├── repository/
│   │   │           ├── security/
│   │   │           ├── service/
│   │   │           └── StudentBagApplication.java
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
