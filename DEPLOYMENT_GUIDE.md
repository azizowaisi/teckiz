# Teckiz Deployment Guide

This guide covers deployment of the Teckiz application to production environments.

## Prerequisites

- Java 23 (or Java 21 LTS) installed
- Node.js 18+ and npm installed
- PostgreSQL database
- AWS account (for S3 and SES)
- Web server (Nginx/Apache) for frontend
- Application server (or Docker) for backend

## Backend Deployment

### Option 1: JAR File Deployment

1. **Build the application:**
```bash
cd backend
mvn clean package -DskipTests
```

2. **Create production configuration:**
```bash
cp src/main/resources/application.properties src/main/resources/application-prod.properties
```

3. **Update production properties:**
```properties
spring.profiles.active=prod
spring.datasource.url=jdbc:postgresql://prod-db-host:5432/teckiz
spring.datasource.username=prod_user
spring.datasource.password=secure_password
jwt.secret=production-secret-key-minimum-256-bits
aws.s3.bucket-name=prod-bucket-name
aws.ses.from-email=noreply@yourdomain.com
```

4. **Run the application:**
```bash
java -jar -Dspring.profiles.active=prod target/teckiz-0.0.1-SNAPSHOT.jar
```

### Option 2: Docker Deployment

1. **Create Dockerfile:**
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/teckiz-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

2. **Build Docker image:**
```bash
docker build -t teckiz-backend:latest .
```

3. **Run container:**
```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/teckiz \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=pass \
  -e JWT_SECRET=your-secret \
  teckiz-backend:latest
```

### Option 3: Systemd Service

1. **Create service file** `/etc/systemd/system/teckiz.service`:
```ini
[Unit]
Description=Teckiz Backend Application
After=network.target

[Service]
Type=simple
User=teckiz
WorkingDirectory=/opt/teckiz/backend
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /opt/teckiz/backend/target/teckiz-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

2. **Enable and start service:**
```bash
sudo systemctl enable teckiz
sudo systemctl start teckiz
sudo systemctl status teckiz
```

## Frontend Deployment

### Build for Production

1. **Update environment:**
```bash
cd frontend
cp src/environments/environment.ts src/environments/environment.prod.ts
```

2. **Update production API URL:**
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.yourdomain.com/api'
};
```

3. **Build the application:**
```bash
ng build --configuration production
```

4. **Output will be in** `dist/teckiz/`

### Nginx Configuration

1. **Install Nginx:**
```bash
sudo apt-get update
sudo apt-get install nginx
```

2. **Create configuration** `/etc/nginx/sites-available/teckiz`:
```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    root /var/www/teckiz/dist/teckiz;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
}
```

3. **Enable site:**
```bash
sudo ln -s /etc/nginx/sites-available/teckiz /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### Apache Configuration

1. **Create virtual host** `/etc/apache2/sites-available/teckiz.conf`:
```apache
<VirtualHost *:80>
    ServerName yourdomain.com
    DocumentRoot /var/www/teckiz/dist/teckiz

    <Directory /var/www/teckiz/dist/teckiz>
        Options Indexes FollowSymLinks
        AllowOverride All
        Require all granted
    </Directory>

    # Proxy API requests
    ProxyPass /api http://localhost:8080/api
    ProxyPassReverse /api http://localhost:8080/api

    # Enable rewrite for Angular routing
    RewriteEngine On
    RewriteBase /
    RewriteRule ^index\.html$ - [L]
    RewriteCond %{REQUEST_FILENAME} !-f
    RewriteCond %{REQUEST_FILENAME} !-d
    RewriteRule . /index.html [L]
</VirtualHost>
```

2. **Enable modules and site:**
```bash
sudo a2enmod proxy proxy_http rewrite
sudo a2ensite teckiz
sudo systemctl reload apache2
```

## SSL/HTTPS Setup

### Using Let's Encrypt (Certbot)

1. **Install Certbot:**
```bash
sudo apt-get install certbot python3-certbot-nginx
```

2. **Obtain certificate:**
```bash
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

3. **Auto-renewal:**
```bash
sudo certbot renew --dry-run
```

## Database Setup

### PostgreSQL Production Database

1. **Create database:**
```sql
CREATE DATABASE teckiz_prod;
CREATE USER teckiz_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE teckiz_prod TO teckiz_user;
```

2. **Run migrations:**
The application will auto-create tables on first run with `spring.jpa.hibernate.ddl-auto=update`

For production, consider using Flyway or Liquibase for versioned migrations.

## AWS Configuration

### S3 Bucket Setup

1. **Create S3 bucket:**
```bash
aws s3 mb s3://teckiz-media-prod --region us-east-1
```

2. **Configure CORS:**
```json
{
  "CORSRules": [
    {
      "AllowedOrigins": ["https://yourdomain.com"],
      "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
      "AllowedHeaders": ["*"],
      "MaxAgeSeconds": 3000
    }
  ]
}
```

3. **Set bucket policy for public read access (if needed):**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::teckiz-media-prod/*"
    }
  ]
}
```

### SES Configuration

1. **Verify domain:**
- Go to AWS SES Console
- Add and verify your domain
- Add DNS records to your domain

2. **Request production access** (if in sandbox mode)
- Submit request in SES Console
- Wait for approval

3. **Configure IAM user:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ses:SendEmail",
        "ses:SendRawEmail"
      ],
      "Resource": "*"
    }
  ]
}
```

## Environment Variables

### Backend Environment Variables

```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/teckiz
export SPRING_DATASOURCE_USERNAME=user
export SPRING_DATASOURCE_PASSWORD=password
export JWT_SECRET=your-secret-key
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_S3_BUCKET_NAME=your-bucket
export AWS_S3_REGION=us-east-1
export AWS_SES_REGION=us-east-1
export AWS_SES_FROM_EMAIL=noreply@yourdomain.com
```

## Monitoring and Logging

### Application Logs

1. **Configure log rotation:**
```bash
sudo logrotate -d /etc/logrotate.d/teckiz
```

2. **Log file location:**
```
/var/log/teckiz/application.log
```

### Health Checks

The application exposes health endpoints:
```
GET /actuator/health
```

### Monitoring Tools

Consider integrating:
- **Prometheus** for metrics
- **Grafana** for visualization
- **ELK Stack** for log aggregation
- **Sentry** for error tracking

## Backup Strategy

### Database Backups

1. **Automated daily backup:**
```bash
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -U teckiz_user teckiz_prod > /backups/teckiz_$DATE.sql
```

2. **Schedule with cron:**
```bash
0 2 * * * /path/to/backup-script.sh
```

### File Backups

S3 buckets should have versioning enabled and lifecycle policies for old versions.

## Security Checklist

- [ ] Change all default passwords
- [ ] Use strong JWT secret (minimum 256 bits)
- [ ] Enable HTTPS/SSL
- [ ] Configure firewall rules
- [ ] Set up rate limiting
- [ ] Enable database encryption
- [ ] Regular security updates
- [ ] Configure CORS properly
- [ ] Use environment variables for secrets
- [ ] Enable audit logging

## Troubleshooting

### Backend won't start
- Check Java version: `java -version`
- Check database connectivity
- Review application logs
- Verify environment variables

### Frontend not loading
- Check Nginx/Apache configuration
- Verify file permissions
- Check browser console for errors
- Verify API URL in environment

### Database connection issues
- Verify database is running
- Check connection string
- Verify user permissions
- Check firewall rules

## Support

For deployment issues, check:
- Application logs
- Server logs
- Database logs
- Network connectivity

---

**Last Updated**: 2024

