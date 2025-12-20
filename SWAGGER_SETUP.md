# Swagger/OpenAPI Setup Guide

## Overview

The Teckiz API now uses Swagger (SpringDoc OpenAPI) for interactive API documentation. This provides an automatically generated, interactive API documentation interface.

## Accessing Swagger UI

Once the backend is running, access Swagger UI at:

```
http://localhost:8080/api/swagger-ui.html
```

Or alternatively:

```
http://localhost:8080/swagger-ui/index.html
```

## API Documentation JSON

The OpenAPI specification is available at:

```
http://localhost:8080/api/api-docs
```

Or:

```
http://localhost:8080/v3/api-docs
```

## Features

### Interactive API Testing
- Test all endpoints directly from the browser
- See request/response examples
- Try out different parameters

### Authentication Support
- JWT Bearer token authentication is configured
- Click "Authorize" button in Swagger UI
- Enter your JWT token to test protected endpoints

### Organized by Tags
- APIs are grouped by modules:
  - Authentication
  - SuperAdmin - Companies
  - SuperAdmin - Users
  - Website Admin
  - Education Admin
  - Journal Admin
  - Public API

## Configuration

### OpenAPI Configuration
Located in: `backend/src/main/java/com/teckiz/config/OpenApiConfig.java`

### Application Properties
Swagger configuration in: `backend/src/main/resources/application.properties`

```properties
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
```

## Adding Swagger Annotations

### Controller Level
```java
@Tag(name = "Module Name", description = "Description of the module")
@SecurityRequirement(name = "bearer-jwt")
public class YourController {
    // ...
}
```

### Method Level
```java
@Operation(summary = "Brief summary", description = "Detailed description")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
@GetMapping("/endpoint")
public ResponseEntity<?> yourMethod() {
    // ...
}
```

### Request/Response Models
Swagger automatically detects DTOs and entities. For better documentation, add:

```java
@Schema(description = "User information")
public class UserRequest {
    @Schema(description = "User email", example = "user@example.com", required = true)
    private String email;
    
    // ...
}
```

## Security

Swagger UI endpoints are configured to be publicly accessible (no authentication required) for development. In production, you may want to restrict access:

```java
.requestMatchers("/swagger-ui/**", "/api-docs/**").hasRole("ADMIN")
```

## Benefits

1. **Automatic Documentation**: No need to manually maintain API docs
2. **Interactive Testing**: Test APIs directly from the browser
3. **Always Up-to-Date**: Documentation is generated from code
4. **Standard Format**: OpenAPI 3.0 standard
5. **Client Generation**: Can generate client SDKs from the spec

## Next Steps

1. Add Swagger annotations to all controllers
2. Document all DTOs with `@Schema` annotations
3. Add examples to request/response models
4. Configure production security if needed

---

**Note**: The static `API_DOCUMENTATION.md` file can be removed or kept as a reference, but Swagger UI is now the primary API documentation source.

