# Teckiz API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication

All protected endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## Authentication Endpoints

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe",
    "roles": ["COMPANY_ADMIN"]
  }
}
```

## SuperAdmin Endpoints

### Companies

#### List Companies
```http
GET /superadmin/companies?page=0&size=20
```

#### Get Company
```http
GET /superadmin/companies/{companyKey}
```

#### Create Company
```http
POST /superadmin/companies
Content-Type: application/json

{
  "name": "Company Name",
  "email": "company@example.com",
  "phone": "+1234567890",
  "address": "123 Main St"
}
```

#### Update Company
```http
PUT /superadmin/companies/{companyKey}
Content-Type: application/json

{
  "name": "Updated Company Name"
}
```

#### Delete Company
```http
DELETE /superadmin/companies/{companyKey}
```

### Users

#### List Users
```http
GET /superadmin/users?page=0&size=20&search=john
```

#### Get User
```http
GET /superadmin/users/{userKey}
```

#### Create User
```http
POST /superadmin/users
Content-Type: application/json

{
  "email": "user@example.com",
  "name": "John Doe",
  "password": "password123",
  "companyId": 1,
  "roleIds": [1, 2]
}
```

#### Update User
```http
PUT /superadmin/users/{userKey}
Content-Type: application/json

{
  "name": "John Updated",
  "roleIds": [1]
}
```

#### Delete User
```http
DELETE /superadmin/users/{userKey}
```

## Website Admin Endpoints

### Pages

#### List Pages
```http
GET /website/admin/pages?page=0&size=20&published=true
```

#### Get Page
```http
GET /website/admin/pages/{pageKey}
```

#### Create Page
```http
POST /website/admin/pages
Content-Type: application/json

{
  "title": "About Us",
  "slug": "about-us",
  "content": "<p>Page content</p>",
  "published": true
}
```

#### Update Page
```http
PUT /website/admin/pages/{pageKey}
Content-Type: application/json

{
  "title": "Updated Title",
  "content": "<p>Updated content</p>"
}
```

#### Delete Page
```http
DELETE /website/admin/pages/{pageKey}
```

### News

#### List News
```http
GET /website/admin/news?page=0&size=20&published=true
```

#### Get News Article
```http
GET /website/admin/news/{newsKey}
```

#### Create News Article
```http
POST /website/admin/news
Content-Type: application/json

{
  "title": "News Title",
  "slug": "news-title",
  "content": "<p>News content</p>",
  "newsTypeId": 1,
  "published": true
}
```

### Events

#### List Events
```http
GET /website/admin/events?page=0&size=20&published=true
```

#### Create Event
```http
POST /website/admin/events
Content-Type: application/json

{
  "title": "Event Title",
  "slug": "event-title",
  "description": "Event description",
  "startDate": "2024-01-01T10:00:00",
  "endDate": "2024-01-01T18:00:00",
  "published": true
}
```

### Albums

#### List Albums
```http
GET /website/admin/albums?page=0&size=20
```

#### Create Album
```http
POST /website/admin/albums
Content-Type: application/json

{
  "title": "Album Title",
  "slug": "album-title",
  "description": "Album description",
  "published": true
}
```

### Media Library

#### List Media
```http
GET /website/admin/media?page=0&size=20&type=image
```

#### Upload Media
```http
POST /website/admin/media/upload
Content-Type: multipart/form-data

file: <file>
title: "Media Title"
description: "Media Description"
```

## Education Admin Endpoints

### Facilities

#### List Facilities
```http
GET /education/admin/facilities?page=0&size=20&published=true
```

#### Create Facility
```http
POST /education/admin/facilities
Content-Type: application/json

{
  "title": "Library",
  "slug": "library",
  "description": "Main library facility",
  "published": true
}
```

### Stories

#### List Stories
```http
GET /education/admin/stories?page=0&size=20&storyTypeId=1
```

#### Create Story
```http
POST /education/admin/stories
Content-Type: application/json

{
  "title": "Success Story",
  "slug": "success-story",
  "content": "Story content",
  "storyTypeId": 1,
  "published": true
}
```

### Program Levels

#### List Program Levels
```http
GET /education/admin/program-levels?page=0&size=20
```

#### Create Program Level
```http
POST /education/admin/program-levels
Content-Type: application/json

{
  "title": "Bachelor's Degree",
  "slug": "bachelors-degree",
  "description": "Undergraduate program",
  "published": true
}
```

## Journal Admin Endpoints

### Research Journals

#### List Journals
```http
GET /journal/admin/research-journals?page=0&size=20
```

#### Create Journal
```http
POST /journal/admin/research-journals
Content-Type: application/json

{
  "title": "Journal Title",
  "slug": "journal-title",
  "description": "Journal description",
  "published": true
}
```

### Research Articles

#### List Articles
```http
GET /journal/admin/research-articles?journalId=1&page=0&size=20
```

#### Create Article
```http
POST /journal/admin/research-articles
Content-Type: application/json

{
  "title": "Article Title",
  "slug": "article-title",
  "abstract": "Article abstract",
  "researchJournalId": 1,
  "published": true
}
```

## Public API Endpoints

### Public Pages
```http
GET /publicapi/pages/{slug}
```

### Public News
```http
GET /publicapi/news?page=0&size=20
GET /publicapi/news/{slug}
```

### Public Events
```http
GET /publicapi/events?page=0&size=20&upcoming=true
GET /publicapi/events/{slug}
```

### Public Facilities
```http
GET /publicapi/facilities?page=0&size=20
GET /publicapi/facilities/{slug}
```

### Public Research Articles
```http
GET /publicapi/research-articles?journalId=1&page=0&size=20
GET /publicapi/research-articles/{slug}
```

## Response Format

### Success Response
```json
{
  "message": "Operation successful",
  "data": { ... }
}
```

### Error Response
```json
{
  "error": "Error message",
  "status": 400,
  "timestamp": "2024-01-01T12:00:00"
}
```

## Pagination

Most list endpoints support pagination:

```
?page=0&size=20
```

**Response:**
```json
{
  "items": [...],
  "currentPage": 0,
  "totalPages": 5,
  "totalItems": 100,
  "pageSize": 20
}
```

## Status Codes

- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Invalid request
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Rate Limiting

API requests are rate-limited to prevent abuse. Limits:
- Authenticated users: 1000 requests/hour
- Public endpoints: 100 requests/hour

## Versioning

Current API version: `v1`

Future versions will be available at:
```
/api/v2/...
```

