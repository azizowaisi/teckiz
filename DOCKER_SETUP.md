# Docker Setup Guide

This guide explains how to set up and run the Teckiz application using Docker.

## Prerequisites

- Docker Desktop (or Docker Engine + Docker Compose)
- At least 4GB of available RAM
- At least 10GB of available disk space

## Quick Start

### Development Environment

1. **Start all services:**
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

2. **View logs:**
   ```bash
   docker-compose -f docker-compose.dev.yml logs -f
   ```

3. **Stop all services:**
   ```bash
   docker-compose -f docker-compose.dev.yml down
   ```

4. **Stop and remove volumes (clean slate):**
   ```bash
   docker-compose -f docker-compose.dev.yml down -v
   ```

### Production Environment

1. **Create `.env` file:**
   ```bash
   cp .env.example .env
   # Edit .env with your production values
   ```

2. **Start all services:**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

3. **View logs:**
   ```bash
   docker-compose -f docker-compose.prod.yml logs -f
   ```

## Services

### 1. Database (MySQL 8.0)

- **Container:** `teckiz-database`
- **Port:** `3306` (mapped to host)
- **Default Credentials:**
  - Root Password: `rootpassword` (dev) / `${MYSQL_ROOT_PASSWORD}` (prod)
  - Database: `teckiz`
  - User: `teckiz`
  - Password: `teckizpassword` (dev) / `${MYSQL_PASSWORD}` (prod)

**Access:**
```bash
# From host
mysql -h localhost -P 3306 -u teckiz -p teckiz

# From within Docker network
mysql -h database -u teckiz -p teckiz
```

### 2. Backend (Spring Boot)

- **Container:** `teckiz-backend`
- **Port:** `8080` (mapped to host)
- **Health Check:** `http://localhost:8080/api/actuator/health`
- **API Docs:** `http://localhost:8080/api/swagger-ui.html`

**Environment Variables:**
- `DATABASE_URL`: MySQL connection string
- `JWT_SECRET`: JWT signing secret
- `AWS_ACCESS_KEY`: AWS access key (for S3/SES)
- `AWS_SECRET_KEY`: AWS secret key
- `AWS_S3_BUCKET`: S3 bucket name

### 3. Frontend (Angular + Nginx)

- **Container:** `teckiz-frontend`
- **Port:** `4200` (dev) / `80` (prod)
- **URL:** `http://localhost:4200` (dev) / `http://localhost` (prod)

**Features:**
- Serves Angular application
- Nginx reverse proxy
- Gzip compression
- Static asset caching
- Angular routing support

## Docker Compose Files

### `docker-compose.yml`
Base configuration file. Can be used with environment variables.

### `docker-compose.dev.yml`
Development configuration with:
- Auto-restart on failure
- Development database credentials
- Hot reload support (if configured)
- SQL logging enabled
- JPA DDL auto-update

### `docker-compose.prod.yml`
Production configuration with:
- Always restart policy
- Environment variables from `.env`
- JPA DDL validation (no auto-update)
- SSL enabled for database
- Optimized settings

## Environment Variables

Create a `.env` file in the project root for production:

```env
# Database
MYSQL_ROOT_PASSWORD=your_secure_root_password
MYSQL_DATABASE=teckiz
MYSQL_USER=teckiz
MYSQL_PASSWORD=your_secure_password
MYSQL_PORT=3306

# Backend
BACKEND_PORT=8080
JWT_SECRET=your_jwt_secret_key_minimum_256_bits
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=false

# AWS
AWS_REGION=us-east-1
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_S3_BUCKET=your-s3-bucket-name

# Email
EMAIL_FROM_ADDRESS=noreply@teckiz.com
EMAIL_FROM_NAME=Teckiz

# Frontend
FRONTEND_PORT=80
API_URL=https://api.teckiz.com/api
```

## Common Commands

### Build Images

```bash
# Build all services
docker-compose build

# Build specific service
docker-compose build backend

# Build without cache
docker-compose build --no-cache
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend

# Last 100 lines
docker-compose logs --tail=100 backend
```

### Execute Commands

```bash
# Access backend container shell
docker-compose exec backend sh

# Access database
docker-compose exec database mysql -u teckiz -p teckiz

# Run migrations (if using Flyway)
docker-compose exec backend java -jar app.jar --spring.profiles.active=migration
```

### Database Operations

```bash
# Backup database
docker-compose exec database mysqldump -u teckiz -p teckiz > backup.sql

# Restore database
docker-compose exec -T database mysql -u teckiz -p teckiz < backup.sql

# Access MySQL CLI
docker-compose exec database mysql -u teckiz -p teckiz
```

### Clean Up

```bash
# Stop and remove containers
docker-compose down

# Stop and remove containers + volumes
docker-compose down -v

# Remove all images
docker-compose down --rmi all

# Remove everything including volumes
docker-compose down -v --rmi all
```

## Health Checks

All services include health checks:

- **Database:** MySQL ping
- **Backend:** HTTP GET to `/api/actuator/health`
- **Frontend:** HTTP GET to `/health`

Check health status:
```bash
docker-compose ps
```

## Troubleshooting

### Backend won't start

1. **Check database connection:**
   ```bash
   docker-compose logs database
   docker-compose logs backend
   ```

2. **Verify database is healthy:**
   ```bash
   docker-compose ps
   ```

3. **Check environment variables:**
   ```bash
   docker-compose exec backend env | grep DATABASE
   ```

### Frontend can't connect to backend

1. **Check backend is running:**
   ```bash
   curl http://localhost:8080/api/actuator/health
   ```

2. **Verify API_URL in frontend:**
   ```bash
   docker-compose exec frontend env | grep API_URL
   ```

3. **Check network connectivity:**
   ```bash
   docker-compose exec frontend ping backend
   ```

### Database connection issues

1. **Check database logs:**
   ```bash
   docker-compose logs database
   ```

2. **Verify credentials:**
   ```bash
   docker-compose exec database mysql -u teckiz -p
   ```

3. **Check network:**
   ```bash
   docker network inspect teckiz-network
   ```

### Port conflicts

If ports are already in use, modify ports in `docker-compose.yml`:

```yaml
services:
  backend:
    ports:
      - "8081:8080"  # Change 8080 to 8081
```

## Production Deployment

1. **Set up environment variables:**
   ```bash
   cp .env.example .env
   # Edit .env with production values
   ```

2. **Build production images:**
   ```bash
   docker-compose -f docker-compose.prod.yml build
   ```

3. **Start services:**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

4. **Set up reverse proxy (Nginx/Traefik):**
   - Configure SSL certificates
   - Set up domain names
   - Configure load balancing if needed

5. **Set up backups:**
   - Database backups (cron job)
   - Volume backups
   - Application logs

## Security Considerations

1. **Change default passwords** in production
2. **Use secrets management** (Docker Secrets, AWS Secrets Manager)
3. **Enable SSL/TLS** for database connections
4. **Restrict network access** (don't expose database port publicly)
5. **Use read-only volumes** where possible
6. **Regular security updates** for base images

## Performance Optimization

1. **Resource limits:**
   ```yaml
   services:
     backend:
       deploy:
         resources:
           limits:
             cpus: '2'
             memory: 2G
   ```

2. **Database connection pooling** (configured in application.yml)
3. **Nginx caching** for static assets
4. **CDN** for frontend assets in production

## Monitoring

- **Health checks:** Built-in via Actuator
- **Logs:** `docker-compose logs`
- **Metrics:** `/api/actuator/metrics` (requires auth)
- **Resource usage:** `docker stats`

---

For more information, see the main [README.md](./README.md).

