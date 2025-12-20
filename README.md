# Teckiz - Multi-Tenant Content Management System

A comprehensive multi-tenant CMS platform migrated from Symfony to Spring Boot (backend) and Angular (frontend).

## 🚀 Project Overview

Teckiz is a full-featured content management system designed for educational institutions and research organizations. It provides multi-tenant architecture with company-based data isolation, role-based access control, and comprehensive content management capabilities.

## 📋 Features

### Core Features

- **Multi-Tenancy**: Company-based data isolation with module-level access control
- **Authentication & Authorization**: JWT-based authentication with role-based access control (RBAC)
- **Content Management**: Full CRUD operations for all content types
- **File Management**: AWS S3 integration for file storage with image processing
- **Email Services**: AWS SES integration for transactional emails
- **Responsive Design**: Modern, mobile-friendly UI built with Angular

### Modules

#### SuperAdmin Module
- User management across all companies
- Company management and configuration
- Module access control
- Email template management
- Invoice and billing management
- Notification request queuing

#### Website Module
- Web pages management
- News articles with types
- Events calendar
- Photo albums
- Contact form submissions
- Email subscribers
- Media library with file upload
- Widgets with content management
- Notification center

#### Education Module
- Facilities management
- Success stories
- Skills showcase
- Principal's message
- Program management (Levels, Courses, Classes, Terms)
- Type management for categorization

#### Journal Module
- Research journals with volumes
- Research articles with authors
- Index journals with volumes
- Index articles
- Article type management

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: JPA/Hibernate
- **Security**: Spring Security + JWT
- **Cloud Services**: AWS S3 (storage), AWS SES (email)
- **Image Processing**: imgscalr-lib
- **Build Tool**: Maven

### Frontend
- **Framework**: Angular 17+ (Standalone components)
- **Language**: TypeScript
- **HTTP Client**: Angular HttpClient
- **Reactive Programming**: RxJS
- **Build Tool**: Angular CLI
- **Styling**: Component-scoped CSS

## 📁 Project Structure

```
teckiz/
├── backend/                 # Spring Boot application
│   ├── src/main/java/com/teckiz/
│   │   ├── controller/     # REST controllers
│   │   ├── entity/          # JPA entities
│   │   ├── repository/     # Data repositories
│   │   ├── service/         # Business logic
│   │   ├── dto/             # Data transfer objects
│   │   └── config/         # Configuration classes
│   └── src/main/resources/
│       └── application.properties
│
├── frontend/                # Angular application
│   ├── src/app/
│   │   ├── core/            # Core services, models, guards
│   │   ├── features/        # Feature modules
│   │   │   ├── super-admin/ # SuperAdmin components
│   │   │   ├── website/     # Website management
│   │   │   ├── education/   # Education module
│   │   │   └── journal/     # Journal module
│   │   └── shared/          # Shared components
│   └── src/environments/    # Environment configuration
│
└── docs/                    # Documentation
```

## 🚦 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js 18+ and npm
- PostgreSQL (or your preferred database)
- AWS account (for S3 and SES)

### Backend Setup

1. Navigate to the backend directory:
```bash
cd backend
```

2. Configure database in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/teckiz
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Configure AWS credentials:
```properties
aws.s3.bucket-name=your-bucket-name
aws.s3.region=us-east-1
aws.ses.region=us-east-1
```

4. Build and run:
```bash
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Configure API URL in `src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

4. Start the development server:
```bash
ng serve
```

The frontend will start on `http://localhost:4200`

## 🔐 Authentication

### Default SuperAdmin Account

After initial setup, create a SuperAdmin account:
- Email: `admin@teckiz.com`
- Password: (set during first setup)

### User Roles

- **SUPER_ADMIN**: Full system access
- **COMPANY_ADMIN**: Company-level administration
- **COMPANY_AUTHOR**: Content creation and editing
- **COMPANY_VIEWER**: Read-only access

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

### Endpoints Structure

- `/auth/*` - Authentication endpoints
- `/superadmin/*` - SuperAdmin operations
- `/website/admin/*` - Website management
- `/education/admin/*` - Education module
- `/journal/admin/*` - Journal module
- `/publicapi/*` - Public-facing APIs

## 🗄️ Database Schema

The application uses JPA entities with automatic schema generation. Key entities include:

- **User, Company, Module, Role** - Core system entities
- **WebPage, WebNews, WebEvent, WebAlbum** - Website content
- **Facility, Story, Skill, PrincipalMessage** - Education content
- **ResearchJournal, ResearchArticle** - Journal content
- **Notification, EmailTemplate** - System entities

## 🔧 Configuration

### Environment Variables

#### Backend
- `SPRING_DATASOURCE_URL` - Database connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `JWT_SECRET` - JWT signing secret
- `AWS_ACCESS_KEY_ID` - AWS access key
- `AWS_SECRET_ACCESS_KEY` - AWS secret key

#### Frontend
- `API_URL` - Backend API URL

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
ng test
```

## 📦 Building for Production

### Backend
```bash
cd backend
mvn clean package
java -jar target/teckiz-0.0.1-SNAPSHOT.jar
```

### Frontend
```bash
cd frontend
ng build --configuration production
```

Output will be in `frontend/dist/`

## 🚢 Deployment

### Backend Deployment
- Deploy the JAR file to your server
- Configure environment variables
- Set up database connection
- Configure AWS credentials

### Frontend Deployment
- Build the production bundle
- Deploy to a web server (Nginx, Apache, etc.)
- Configure API URL for production environment

## 📝 Development Guidelines

### Code Style
- Follow Java naming conventions for backend
- Follow TypeScript/Angular style guide for frontend
- Use meaningful variable and method names
- Add comments for complex logic

### Git Workflow
- Create feature branches for new features
- Write descriptive commit messages
- Keep commits atomic and focused

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Write/update tests
5. Submit a pull request

## 📄 License

[Add your license information here]

## 👥 Authors

- Development Team

## 🙏 Acknowledgments

- Spring Boot team
- Angular team
- All contributors

## 📞 Support

For support, email support@teckiz.com or create an issue in the repository.

---

**Version**: 1.0.0  
**Status**: Production Ready  
**Last Updated**: 2024
