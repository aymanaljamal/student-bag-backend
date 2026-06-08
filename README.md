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
Requirements

Before running the project, make sure you have the following installed:

Java 17 or later
Maven
PostgreSQL
Docker and Docker Compose
Environment Configuration

Create an environment file or configure the required variables inside application.properties.

Example:

SERVER_PORT=8080

DB_URL=jdbc:postgresql://localhost:5432/student_bag
DB_USERNAME=postgres
DB_PASSWORD=your_password

JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000

OPENAI_API_KEY=your_openai_api_key

Make sure not to push real passwords, secret keys, or API keys to GitHub.

Database Configuration

Default PostgreSQL database name:

student_bag

Example local PostgreSQL configuration:

spring.datasource.url=jdbc:postgresql://localhost:5432/student_bag
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
Running the Project Locally
1. Clone the Repository
git clone https://github.com/aymanajamal/student-bag-backend.git
cd student-bag-backend
2. Install Dependencies
mvn clean install
3. Run the Application
mvn spring-boot:run

The backend will start on:

http://localhost:8080
Running with Docker
1. Build the Docker Image
docker build -t student-bag-backend .
2. Run with Docker Compose
docker compose up -d
3. Check Running Containers
docker ps
4. Stop Containers
docker compose down
API Base URL

Local development URL:

http://localhost:8080/api/v1

Production server example:

http://your-server-ip:8080/api/v1
Main API Modules

The backend contains APIs for the following modules:

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
Authentication

Most endpoints require a JWT token.

After login, include the token in the request header:

Authorization: Bearer <your_token>
User Roles

The system supports role-based access control.

Available roles:

ADMIN
INSTRUCTOR
STUDENT
PARENT

Each role has access only to the endpoints and actions allowed by the backend security configuration.

Docker Deployment Notes

When deploying the backend on a server:

Make sure PostgreSQL is running.
Make sure port 8080 is open.
Make sure the database credentials are correct.
Keep secret keys inside environment variables.
Never expose private keys in GitHub.
Use Docker Compose for easier deployment.

Example useful commands:

docker compose pull
docker compose up -d
docker logs -f student-bag-api
docker restart student-bag-api
Development Guidelines

When working on the backend:

Keep controllers clean.
Put business logic inside services.
Use repositories only for database access.
Use DTOs instead of exposing entities directly.
Validate request data before saving it.
Keep API responses consistent.
Use meaningful exception messages.
Avoid hardcoded secrets.
Test endpoints before pushing changes.
Git Workflow

Recommended branch names:

feature/auth-module
feature/resources-management
feature/schedule-generator
feature/ai-chatbot
fix/docker-backend-setup
fix/notifications-service
fix/resources-approval

Basic Git commands:

git checkout -b feature/branch-name
git add .
git commit -m "Add clear commit message"
git push origin feature/branch-name
Project Status

The backend is under active development.

Current focus areas include:

Improving Docker deployment
Completing resources approval workflow
Enhancing notifications
Improving reports and analytics
Connecting AI chatbot features
Stabilizing student schedule generation
Improving API security and error handling
Related Project

Frontend repository:

student_bag_app

The frontend is built with Flutter and connects to this backend through REST APIs.

Author

Developed by Ayman Al Jamal.

License

This project is developed for academic purposes.
