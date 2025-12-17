# Migration Guide: Symfony 6.4 to Spring Boot + Angular

This document outlines the migration from Symfony 6.4 to Spring Boot (backend) and Angular (frontend).

## Architecture Changes

### Backend: Symfony → Spring Boot

| Symfony Component | Spring Boot Equivalent |
|------------------|----------------------|
| Doctrine ORM | JPA/Hibernate |
| Symfony Security | Spring Security |
| Symfony Controllers | Spring REST Controllers |
| Symfony Services | Spring Services |
| Symfony Repositories | JPA Repositories |
| Twig Templates | JSON REST API |
| Symfony Forms | DTOs with Validation |

### Frontend: Twig → Angular

| Symfony Component | Angular Equivalent |
|------------------|-------------------|
| Twig Templates | Angular Components |
| Symfony Forms | Angular Reactive Forms |
| Symfony Routing | Angular Router |
| Symfony Security | JWT + Route Guards |

## Key Differences

### Authentication

**Symfony:**
- Session-based authentication
- Form login with CSRF tokens
- Remember me functionality

**Spring Boot:**
- JWT token-based authentication
- Stateless authentication
- Token stored in localStorage (frontend)

### Database Access

**Symfony:**
- Doctrine ORM with annotations/attributes
- Repository pattern with custom queries

**Spring Boot:**
- JPA/Hibernate with annotations
- Spring Data JPA repositories
- Query methods and custom queries

### API Design

**Symfony:**
- Mixed: HTML responses (Twig) + JSON APIs
- Server-side rendering

**Spring Boot:**
- RESTful JSON API only
- Client-side rendering (Angular)

## Migration Checklist

### Completed ✅

- [x] Spring Boot project structure
- [x] JPA entities (User, Company, Role, Module, etc.)
- [x] Spring Security with JWT
- [x] Authentication endpoints
- [x] Super Admin controllers
- [x] Angular project structure
- [x] Authentication service
- [x] Route guards
- [x] Login component
- [x] Super Admin dashboard

### To Be Migrated

- [ ] Research Article entities and controllers
- [ ] Web Page entities and controllers
- [ ] Journal management
- [ ] File upload handling
- [ ] Email service (AWS SES)
- [ ] Image processing (Liip Imagine → Java equivalent)
- [ ] All remaining controllers
- [ ] Form validation
- [ ] Error handling
- [ ] Logging configuration
- [ ] Testing

## Database Compatibility

The Spring Boot application uses the same database schema as the Symfony application. The JPA entities are mapped to the existing tables, so you can use the same database without migration.

**Important:** Make sure the database connection settings match your Symfony configuration.

## Running the Application

### Backend

1. Set up MySQL database
2. Configure `application.yml` with database credentials
3. Set JWT secret (use environment variable `JWT_SECRET`)
4. Run: `mvn spring-boot:run`

### Frontend

1. Install dependencies: `npm install`
2. Configure API URL in `environment.ts`
3. Run: `npm start`

## Testing the Migration

1. **Backend API:**
   - Test login endpoint: `POST /api/auth/login`
   - Test protected endpoints with JWT token

2. **Frontend:**
   - Login with existing credentials
   - Navigate to Super Admin dashboard
   - Verify user and module listings

## Common Issues

### JWT Token Issues
- Ensure JWT secret is properly configured
- Check token expiration settings
- Verify token is sent in Authorization header

### CORS Issues
- Backend CORS is configured for `http://localhost:4200`
- Adjust if using different port or domain

### Database Connection
- Verify MySQL is running
- Check database credentials
- Ensure database exists

## Next Steps

1. Migrate remaining entities from Symfony
2. Implement remaining controllers
3. Add file upload functionality
4. Implement email service
5. Add comprehensive error handling
6. Write unit and integration tests
7. Set up CI/CD pipeline
8. Deploy to production

