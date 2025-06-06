# Leave Management System

A comprehensive leave management system built with Spring Boot that allows employees to manage their leave requests and administrators to handle approvals.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher
- SMTP Server (for email notifications)

## Technology Stack

- Spring Boot 3.2.3
- Spring Security with OAuth2
- Spring Data JPA
- PostgreSQL
- WebSocket for real-time notifications
- Thymeleaf for email templates
- JWT for authentication
- H2 Database (for testing)

## Project Setup

1. Clone the repository:
```bash
git clone https://github.com/fab-ryan/leave-management-system-Backend.git
cd Leave-Management-System
```

2. Configure the database:
   - Create a PostgreSQL database named `leave_management`
   - Update the database configuration in `application.properties`

3. Configure Microsoft OAuth2 (for authentication):
   - Register an application in Azure Portal
   - Update the OAuth2 configuration in `application.properties`

4. Configure email settings:
   - Update SMTP settings in `application.properties`

## Configuration Properties

### Database Configuration
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/leave_management
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Security Configuration
```properties
security.jwt.secret=your_jwt_secret_key
security.jwt.expiration=3600000

# Microsoft OAuth2 Configuration
spring.security.oauth2.client.registration.microsoft.client-id=your_client_id
spring.security.oauth2.client.registration.microsoft.client-secret=your_client_secret
# Microsoft Authentication
microsoft.client-id=secret-id
microsoft.client-secret=secret-key
microsoft.redirect-uri=http://localhost:5500/api/auth/microsoft/callback
```

### Other Setups
```
allowed.email.domain=ist.com
leave.management.year=2024
file.upload-dir=./uploads
server.servlet.context-path
server.port=5500
frontend.url=http://localhost:3000

```

### Email Configuration
```properties
spring.mail.host=your_smtp_host
spring.mail.port=587
spring.mail.username=your_email
spring.mail.password=your_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### File Upload Configuration
```properties
file.upload-dir=./uploads
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### WebSocket Configuration
```properties
# WebSocket is enabled by default
# To disable for testing, use the 'test' profile
```

### Database Initialization

```
The application uses PostgreSQL as its database. To initialize the admin user and necessary database structures, follow these steps:

1. Ensure PostgreSQL is running on your system
2. Create a database named `leave_management`
3. Run the initialization script:
 - chmod -x init-admin.sh
 - then run ./init-admin.sh 
```

## Running the Application

1. Build the application:
```bash
mvn clean install
```

2. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:port`

## Testing

1. Run unit tests:
```bash
mvn test
```

2. Run integration tests:
```bash
mvn verify
```

## API Documentation

Once the application is running, you can access the API documentation at:
```
http://localhost:port/swagger-ui/index.html
```

## Features

- User Authentication (Microsoft OAuth2)
- Department Management
- Leave Request Management
- Real-time Notifications
- Email Notifications
- File Upload/Download
- Role-based Access Control
- API Documentation (OpenAPI/Swagger)



## Security Considerations

1. JWT Secret Key:
   - Use a strong, unique secret key
   - Store it securely (e.g., environment variables)
   - Rotate periodically

2. Database Credentials:
   - Use strong passwords
   - Limit database user permissions
   - Use connection pooling

3. OAuth2 Configuration:
   - Keep client secrets secure
   - Use HTTPS in production
   - Configure proper redirect URIs

## Troubleshooting

1. Database Connection Issues:
   - Verify PostgreSQL is running
   - Check database credentials
   - Ensure database exists

2. OAuth2 Authentication Issues:
   - Verify client ID and secret
   - Check redirect URI configuration
   - Ensure proper scopes are configured

3. Email Issues:
   - Verify SMTP settings
   - Check email credentials
   - Ensure proper port access
