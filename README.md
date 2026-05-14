# PVT-15 Backend

Backend for **Skogsjakten**, built with **Spring Boot** and **Java 17**.

## Tech stack
- Java 17
- Spring Boot
- Maven
- MariaDB
- JWT-based authentication

## Getting started

### 1. Clone the repository
```bash
git clone git@github.com:pvt-15/pvt-15-backend.git
cd pvt-15-backend
```

### 2. Configure environment variables
Create a run configuration or environment file with the variables needed by the application, for example:

```bash
DB_URL=jdbc:mariadb://mysql.dsv.su.se:3306/YOUR_DATABASE
DB_USER=YOUR_USERNAME
DB_PASS=YOUR_PASSWORD
JWT_SECRET=YOUR_SECRET
```

Add any other required variables depending on which integrations you are using.

### 3. Run the application
Using Maven wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

You can also run the main application class directly from IntelliJ.

## Project structure
- `src/main/java` - application source code
- `src/test/java` - tests
- `pom.xml` - Maven configuration
- `compose.yaml` - local container setup

## Main features
- Authentication with local login and Google login
- User management
- Image upload and picture handling
- AI-based picture validation and categorization
- Challenges, quizzes and badges
- Gamification with points and levels

## Notes
- Protected endpoints require a backend JWT in:
  `Authorization: Bearer <token>`
- Uploaded images use an `objectKey` flow. Frontend should send `imageObjectKey` to `/pictures`, not a signed image URL.
- Admin endpoints use `ADMIN-KEY` and should not be exposed in the normal frontend app.

## Testing
Run tests with:

```bash
./mvnw test
```

On Windows:

```bash
mvnw.cmd test
```

## API documentation
See the project API documentation for endpoint details, request bodies and expected flows.
