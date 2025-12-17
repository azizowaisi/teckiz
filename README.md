# Teckiz Application

This is a migration of the Symfony 6.4 application to Spring Boot (backend) and Angular (frontend).

## Project Structure

```
teckiz/
├── backend/          # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/teckiz/
│   │   │   │   ├── config/        # Configuration classes
│   │   │   │   ├── controller/    # REST controllers
│   │   │   │   ├── dto/           # Data Transfer Objects
│   │   │   │   ├── entity/        # JPA entities
│   │   │   │   ├── repository/    # JPA repositories
│   │   │   │   ├── security/      # Security configuration
│   │   │   │   └── service/       # Business logic services
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   └── pom.xml
│
└── frontend/         # Angular application
    ├── src/
    │   ├── app/
    │   │   ├── core/              # Core services, guards, interceptors
    │   │   └── features/          # Feature modules
    │   │       ├── auth/          # Authentication
    │   │       ├── super-admin/   # Super admin features
    │   │       └── website/       # Website management
    │   └── environments/
    ├── angular.json
    └── package.json
```

## Backend (Spring Boot)

### Prerequisites
- Java 21
- Maven 3.8+
- MySQL 8.0+

### Setup

1. Configure database in `backend/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/teckiz
    username: your_username
    password: your_password
```

2. Set JWT secret in environment variables or `application.yml`:
```yaml
spring:
  security:
    jwt:
      secret: your-secret-key-change-this-in-production
```

3. Build and run:
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api`

## Frontend (Angular)

### Prerequisites
- Node.js 18+
- npm or yarn

### Setup

1. Install dependencies:
```bash
cd frontend
npm install
```

2. Configure API URL in `src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

3. Run development server:
```bash
npm start
```

The application will be available at `http://localhost:4200`

## Features Migrated

### Backend
- ✅ Spring Boot 3.3.0 with Java 21
- ✅ JPA/Hibernate for database access
- ✅ Spring Security with JWT authentication
- ✅ User and Company entities
- ✅ Role-based access control
- ✅ Super Admin controllers
- ✅ Authentication endpoints

### Frontend
- ✅ Angular 18 with standalone components
- ✅ JWT authentication
- ✅ Route guards
- ✅ HTTP interceptors
- ✅ Login component
- ✅ Super Admin dashboard
- ✅ Users and Modules management

## Migration Status

This is an initial migration. Additional features to be migrated:
- Research Article management
- Web Page management
- Journal management
- File upload handling
- Email services
- Additional controllers and services

## Notes

- The database schema should remain compatible with the existing Symfony application
- JWT tokens are used for stateless authentication
- CORS is configured to allow requests from `http://localhost:4200`
- Password encoding uses BCrypt (compatible with Symfony's bcrypt)

