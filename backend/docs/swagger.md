# Swagger/OpenAPI Configuration Guide

Complete guide to implementing and configuring Swagger/OpenAPI documentation in Spring Boot applications.

## Table of Contents
1. [Overview](#overview)
2. [Dependencies](#dependencies)
3. [OpenAPI Configuration](#openapi-configuration)
4. [API Grouping](#api-grouping)
5. [JWT Authentication Setup](#jwt-authentication-setup)
6. [Controller Annotations](#controller-annotations)
7. [Configuration Properties](#configuration-properties)
8. [Production Deployment](#production-deployment)
9. [Best Practices](#best-practices)
10. [Examples](#examples)

---

## Overview

This project uses **SpringDoc OpenAPI 3** (version 2.8.9) for API documentation. SpringDoc automates the generation of OpenAPI 3 specification and provides an interactive Swagger UI.

### Key Features

✅ **Auto-generated Documentation** - Scans controllers and generates OpenAPI spec
✅ **Interactive API Testing** - Swagger UI with "Try it out" functionality
✅ **JWT Authentication** - Built-in support for Bearer token authentication
✅ **API Grouping** - Organized endpoints by module (Support System, Oil Depot, Shared)
✅ **Customizable UI** - Sorting, filtering, and deep-linking enabled
✅ **Production Ready** - Can be disabled in production for security

### Architecture

```
┌──────────────────────────────────────────────────────────┐
│                  SpringDoc OpenAPI                       │
├──────────────────────────────────────────────────────────┤
│                                                            │
│  ┌──────────────┐         ┌───────────────┐             │
│  │  Controller  │────────▶│  Annotation   │             │
│  │  Classes     │         │  Scanner      │             │
│  └──────────────┘         └───────────────┘             │
│        │                          │                       │
│        │                          ▼                       │
│        │                  ┌───────────────┐             │
│        │                  │  OpenAPI      │             │
│        └─────────────────▶│  Generator    │             │
│                           └───────────────┘             │
│                                   │                       │
│                                   ▼                       │
│                           ┌───────────────┐             │
│                           │  OpenAPI      │             │
│                           │  Spec JSON    │             │
│                           └───────────────┘             │
│                                   │                       │
│                    ┌──────────────┴──────────────┐      │
│                    ▼                             ▼       │
│            ┌───────────────┐          ┌──────────────┐  │
│            │  Swagger UI   │          │  ReDoc UI    │  │
│            │  /swagger-ui  │          │  (optional)  │  │
│            └───────────────┘          └──────────────┘  │
│                                                            │
└──────────────────────────────────────────────────────────┘
```

---

## Dependencies

### Gradle Configuration

**shared/build.gradle:**
```gradle
dependencies {
    // SpringDoc OpenAPI for API documentation
    api 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9'
}
```

**Benefits of version 2.8.9:**
- Full OpenAPI 3.0 support
- Spring Boot 3.x compatibility
- Spring Security 6.x integration
- WebMVC starter includes Swagger UI
- Automatic schema generation
- Built-in validation support

### Maven Equivalent

If using Maven:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.9</version>
</dependency>
```

---

## OpenAPI Configuration

### OpenApiConfig.java

Complete configuration class for OpenAPI/Swagger setup:

```java
package kg.bishkek.fuel.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for OpenAPI/Swagger with JWT authentication support and modular API grouping
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fuel Ecosystem API")
                        .description("Комплексная система управления сетью АЗС")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Fuel Ecosystem Team")
                                .email("support@fueleco.kg"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://fueleco.kg/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080" + contextPath)
                                .description("Development Server"),
                        new Server()
                                .url("https://fs-api-test.kgdigit.tech" + contextPath)
                                .description("Test Server"),
                        new Server()
                                .url("https://fs-api.kgdigit.tech" + contextPath)
                                .description("Production Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from /auth/login endpoint")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * Support System API Group
     * Contains all endpoints related to technical support management
     */
    @Bean
    public GroupedOpenApi supportSystemApi() {
        return GroupedOpenApi.builder()
                .group("support-system")
                .displayName("Система Технической Поддержки")
                .pathsToMatch(
                        "/support/tickets/**",
                        "/support/ticket-comments/**",
                        "/support/routing/**",
                        "/support/escalation/**",
                        "/support/analytics/**",
                        "/support/reports/**",
                        "/support/telegram/**",
                        "/support/notifications/**",
                        "/support/hb/equipment/**"
                )
                .build();
    }

    /**
     * Oil Depot API Group
     * Contains all endpoints related to oil depot management
     */
    @Bean
    public GroupedOpenApi oilDepotApi() {
        return GroupedOpenApi.builder()
                .group("oil-depot")
                .displayName("Модуль Нефтебазы")
                .pathsToMatch("/oil-depot/**")
                .build();
    }

    /**
     * Shared/Common API Group
     * Contains authentication, user management, and shared handbook endpoints
     */
    @Bean
    public GroupedOpenApi sharedApi() {
        return GroupedOpenApi.builder()
                .group("shared")
                .displayName("Общие API Компоненты")
                .pathsToMatch(
                        "/auth/**",
                        "/users/**",
                        "/roles/**",
                        "/gas-stations/**",
                        "/technician-types/**",
                        "/provinces/**",
                        "/regions/**"
                )
                .build();
    }

    /**
     * All APIs Group
     * Combines all endpoints in a single view
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .displayName("All APIs")
                .pathsToMatch("/**")
                .build();
    }
}
```

### Key Configuration Elements

#### 1. OpenAPI Info
```java
.info(new Info()
    .title("Fuel Ecosystem API")          // API title
    .description("System description")     // Description
    .version("1.0.0")                      // Version
    .contact(new Contact()                 // Contact info
        .name("Team Name")
        .email("support@example.com"))
    .license(new License()                 // License info
        .name("Proprietary")))
```

#### 2. Security Scheme (JWT)
```java
.components(new Components()
    .addSecuritySchemes("bearerAuth", new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .description("JWT token from /auth/login")))
```

#### 3. Global Security Requirement
```java
.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
```
This applies JWT authentication to ALL endpoints by default.

#### 4. Server URLs
```java
.servers(List.of(
    new Server().url("http://localhost:8080/api").description("Dev"),
    new Server().url("https://api.prod.com/api").description("Prod")
))
```

---

## API Grouping

### Purpose of Grouping

API grouping organizes endpoints into logical modules for better navigation and documentation clarity.

### Group Configuration

```java
@Bean
public GroupedOpenApi customGroup() {
    return GroupedOpenApi.builder()
            .group("group-id")              // Unique identifier
            .displayName("Display Name")     // UI display name
            .pathsToMatch("/path/**")        // URL patterns to include
            .build();
}
```

### Project Groups

| Group ID | Display Name | Endpoints |
|----------|--------------|-----------|
| `support-system` | Система Технической Поддержки | `/support/**` tickets, comments, routing, analytics |
| `oil-depot` | Модуль Нефтебазы | `/oil-depot/**` fuel operations, tanks, monitoring |
| `shared` | Общие API Компоненты | `/auth/**`, `/users/**`, handbooks |
| `all` | All APIs | `/**` all endpoints combined |

### Accessing Groups

**Swagger UI with groups:**
```
http://localhost:8080/api/swagger-ui.html
```
Select group from dropdown in top-right corner.

---

## JWT Authentication Setup

### Security Configuration

JWT authentication is configured at two levels:

#### 1. Global Level (OpenApiConfig)
```java
.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
```
Applies to all endpoints automatically.

#### 2. Controller Level
```java
@RestController
@SecurityRequirement(name = "bearerAuth")
public class MyController {
    // All endpoints require JWT
}
```

#### 3. Method Level (Override)
```java
@PostMapping("/login")
@Operation(security = {}) // Public endpoint - no auth required
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    // ...
}
```

### Testing with JWT in Swagger UI

**Step 1: Login**
```bash
POST /auth/login
{
  "username": "admin",
  "password": "12345"
}
```

**Step 2: Copy Access Token**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

**Step 3: Authorize in Swagger UI**
1. Click "Authorize" button (top-right)
2. Enter: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
3. Click "Authorize"
4. All subsequent requests include the JWT token

---

## Controller Annotations

### Essential Annotations

#### @Tag - Controller Documentation
```java
@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {
    // ...
}
```

#### @Operation - Endpoint Documentation
```java
@GetMapping("/{id}")
@Operation(
    summary = "Get user by ID",
    description = "Retrieves a user by their unique identifier"
)
public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
    // ...
}
```

#### @Parameter - Path/Query Parameter Documentation
```java
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getUserById(
    @Parameter(description = "User ID", example = "123", required = true)
    @PathVariable Long id
) {
    // ...
}
```

#### @RequestBody - Request Body Documentation
```java
@PostMapping
@Operation(
    summary = "Create user",
    requestBody = @RequestBody(
        required = true,
        description = "User creation data",
        content = @Content(schema = @Schema(implementation = UserRequest.class))
    )
)
public ResponseEntity<UserResponse> createUser(
    @Valid @org.springframework.web.bind.annotation.RequestBody UserRequest request
) {
    // ...
}
```

#### @ApiResponse - Response Documentation
```java
@GetMapping("/{id}")
@Operation(
    summary = "Get user by ID",
    responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    }
)
public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
    // ...
}
```

### Complete Controller Example

```java
package kg.bishkek.fuel.shared.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.bishkek.fuel.shared.dto.user.request.UserRequest;
import kg.bishkek.fuel.shared.dto.user.response.UserResponse;
import kg.bishkek.fuel.shared.dto.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management", description = "APIs for managing system users")
public class UserController extends BaseController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves detailed information about a specific user",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User found successfully",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access denied - insufficient permissions"
            )
        }
    )
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SUPPORT_ADMINISTRATOR')")
    public ResponseEntity<BaseResponse<UserResponse>> getUserById(
        @Parameter(description = "User ID", example = "1", required = true)
        @PathVariable Long id
    ) {
        return success(userService.findById(id));
    }

    @PostMapping
    @Operation(
        summary = "Create new user",
        description = "Creates a new user account with the provided details",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "User creation data",
            content = @Content(schema = @Schema(implementation = UserRequest.class))
        ),
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "User created successfully",
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request data"
            )
        }
    )
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SUPPORT_ADMINISTRATOR')")
    public ResponseEntity<BaseResponse<UserResponse>> createUser(
        @Valid @org.springframework.web.bind.annotation.RequestBody UserRequest request
    ) {
        return success(userService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update user",
        description = "Updates an existing user's information",
        responses = @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        )
    )
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'SUPPORT_ADMINISTRATOR')")
    public ResponseEntity<BaseResponse<UserResponse>> updateUser(
        @Parameter(description = "User ID", example = "1")
        @PathVariable Long id,
        @Valid @org.springframework.web.bind.annotation.RequestBody UserRequest request
    ) {
        return success(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete user",
        description = "Soft deletes a user (sets deleted flag to true)",
        responses = @ApiResponse(responseCode = "204", description = "User deleted successfully")
    )
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Void> deleteUser(
        @Parameter(description = "User ID", example = "1")
        @PathVariable Long id
    ) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

### DTO Schema Annotations

Use `@Schema` on DTOs for better documentation:

```java
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User creation request")
public class UserRequest {

    @Schema(description = "Username (unique)", example = "john.doe", required = true)
    private String username;

    @Schema(description = "User's email address", example = "john.doe@example.com", required = true)
    private String email;

    @Schema(description = "User's full name", example = "John Doe", required = true)
    private String fullName;

    @Schema(description = "Phone number", example = "+996555123456")
    private String phone;

    @Schema(description = "Role IDs to assign", example = "[1, 2]")
    private List<Long> roleIds;
}
```

---

## Configuration Properties

### Development Configuration

**application.properties (development):**
```properties
# =====================================================
# Swagger/OpenAPI Configuration
# =====================================================
springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.operations-sorter=alpha
springdoc.swagger-ui.filter=true
springdoc.swagger-ui.display-request-duration=true
springdoc.swagger-ui.deep-linking=true
springdoc.api-docs.version=openapi_3_0
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/api-docs
```

### Property Explanations

| Property | Value | Description |
|----------|-------|-------------|
| `tags-sorter` | `alpha` | Sort tags alphabetically |
| `operations-sorter` | `alpha` | Sort operations alphabetically |
| `filter` | `true` | Enable search/filter in UI |
| `display-request-duration` | `true` | Show request execution time |
| `deep-linking` | `true` | Enable direct links to operations |
| `api-docs.version` | `openapi_3_0` | OpenAPI specification version |
| `swagger-ui.path` | `/swagger-ui.html` | Swagger UI access path |
| `api-docs.path` | `/api-docs` | OpenAPI JSON spec path |

### Production Configuration

**application-prod.properties:**
```properties
# =====================================================
# Swagger/OpenAPI Configuration - DISABLED for security
# =====================================================
springdoc.swagger-ui.enabled=${SWAGGER_ENABLED:false}
springdoc.api-docs.enabled=${API_DOCS_ENABLED:false}
```

**Security Best Practice:** Disable Swagger in production to prevent API endpoint enumeration.

### Additional Configuration Options

```properties
# Customize Swagger UI
springdoc.swagger-ui.default-models-expand-depth=-1  # Hide schemas by default
springdoc.swagger-ui.doc-expansion=none              # Collapse all operations
springdoc.swagger-ui.disable-swagger-default-url=true

# Package scanning
springdoc.packages-to-scan=kg.bishkek.fuel.shared.controller,kg.bishkek.fuel.oildepot.controller
springdoc.paths-to-match=/api/**,/public/**

# API documentation customization
springdoc.show-actuator=false                        # Hide actuator endpoints
springdoc.model-and-view-allowed=false              # Disable model-view responses
```

---

## Production Deployment

### Disabling Swagger in Production

#### Option 1: Environment Variables
```bash
export SWAGGER_ENABLED=false
export API_DOCS_ENABLED=false
```

#### Option 2: application-prod.properties
```properties
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

#### Option 3: Conditional Bean Configuration
```java
@Configuration
@Profile("!prod")
public class OpenApiConfig {
    // Only active in non-production profiles
}
```

### Security Considerations

**Why disable Swagger in production?**
1. **Endpoint Enumeration** - Exposes all API endpoints
2. **Schema Exposure** - Reveals data structures
3. **Attack Surface** - Provides attackers with API map
4. **Testing Interface** - Allows unauthorized API testing

**Best Practice:** Use environment-specific configurations and disable in production.

### Alternative: Protected Swagger

If Swagger must be available in production:

```java
@Configuration
@EnableMethodSecurity
public class SwaggerSecurityConfig {

    @Bean
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/api-docs/**")
                .hasRole("ADMIN") // Require ADMIN role
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

---

## Best Practices

### 1. Consistent Annotation Usage

**Always use:**
- `@Tag` on controllers
- `@Operation` on public methods
- `@Parameter` on path/query parameters
- `@Schema` on DTOs

### 2. Meaningful Descriptions

**Good:**
```java
@Operation(
    summary = "Create fuel transfer",
    description = "Creates an internal fuel transfer between two tanks within the same oil depot. " +
                  "Validates tank compatibility, fuel type matching, and capacity constraints."
)
```

**Bad:**
```java
@Operation(summary = "Create transfer")
```

### 3. Example Values

Always provide examples:
```java
@Parameter(description = "User ID", example = "123")
@Schema(description = "Username", example = "john.doe")
```

### 4. Response Documentation

Document all possible responses:
```java
@Operation(
    responses = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "500", description = "Server error")
    }
)
```

### 5. Security Annotations

**Public endpoints:**
```java
@PostMapping("/login")
@Operation(security = {}) // Override global security
public ResponseEntity<?> login() { }
```

**Protected endpoints:**
```java
@GetMapping("/users")
@SecurityRequirement(name = "bearerAuth")
public ResponseEntity<?> getUsers() { }
```

### 6. DTO Validation Annotations

Combine with OpenAPI:
```java
@Schema(description = "User email", example = "user@example.com", required = true)
@NotBlank(message = "Email is required")
@Email(message = "Email must be valid")
private String email;
```

### 7. API Versioning

```java
@RestController
@RequestMapping("/v1/users")
@Tag(name = "User Management v1")
public class UserControllerV1 { }

@RestController
@RequestMapping("/v2/users")
@Tag(name = "User Management v2")
public class UserControllerV2 { }
```

---

## Examples

### Complete API Documentation Example

**AuthController.java:**
```java
package kg.bishkek.fuel.shared.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.bishkek.fuel.shared.dto.auth.request.AuthLoginRequest;
import kg.bishkek.fuel.shared.dto.auth.response.AuthResponse;
import kg.bishkek.fuel.shared.dto.common.BaseResponse;
import kg.bishkek.fuel.shared.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Авторизация", description = "API для аутентификации пользователей")
public class AuthController extends BaseController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "Вход в систему",
        description = "Аутентифицирует пользователя по username/email и паролю. " +
                      "Возвращает JWT access и refresh токены.",
        security = {}, // Public endpoint
        requestBody = @RequestBody(
            required = true,
            description = "Данные для аутентификации (username или email + password)",
            content = @Content(schema = @Schema(implementation = AuthLoginRequest.class))
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Успешная аутентификация",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Неверные учетные данные"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Пользователь неактивен"
            )
        }
    )
    public ResponseEntity<BaseResponse<AuthResponse>> login(
        @Valid @org.springframework.web.bind.annotation.RequestBody AuthLoginRequest loginRequest
    ) {
        AuthResponse authResponse = authService.login(loginRequest);
        return success(authResponse);
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Обновление токена",
        description = "Генерирует новую пару access и refresh токенов на основе валидного refresh токена",
        security = {}, // Public endpoint
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Токены успешно обновлены",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Невалидный или истекший refresh токен"
            )
        }
    )
    public ResponseEntity<BaseResponse<AuthResponse>> refreshToken(
        @RequestParam String refreshToken
    ) {
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        return success(authResponse);
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Выход из системы",
        description = "Завершает пользовательскую сессию (клиент должен удалить токены)",
        responses = @ApiResponse(
            responseCode = "200",
            description = "Успешный выход"
        )
    )
    public ResponseEntity<BaseResponse<Void>> logout(
        @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return success(null);
    }

    @GetMapping("/validate")
    @Operation(
        summary = "Валидация токена",
        description = "Проверяет валидность JWT токена и возвращает username",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Токен валиден",
                content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Невалидный токен"
            )
        }
    )
    public ResponseEntity<BaseResponse<String>> validateToken(
        @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        String username = authService.validateTokenAndGetUsername(token);

        if (username != null) {
            return success(username);
        } else {
            return error("Недействительный токен", 401);
        }
    }
}
```

### Oil Depot Controller Example

**FuelTransferController.java:**
```java
package kg.bishkek.fuel.oildepot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.bishkek.fuel.oildepot.dto.fueltransfer.filter.FuelTransferFilter;
import kg.bishkek.fuel.oildepot.dto.fueltransfer.request.FuelTransferRequest;
import kg.bishkek.fuel.oildepot.dto.fueltransfer.response.FuelTransferResponse;
import kg.bishkek.fuel.oildepot.dto.fueltransfer.response.PageFuelTransferResponse;
import kg.bishkek.fuel.oildepot.service.FuelTransferService;
import kg.bishkek.fuel.shared.controller.BaseController;
import kg.bishkek.fuel.shared.dto.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Fuel Transfer operations
 */
@RestController
@RequestMapping("${oildepot.api.base-path}/fuel-transfers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "ОПЕРАЦИИ ПЕРЕМЕЩЕНИЯ", description = "API для управления внутренними перемещениями топлива")
public class FuelTransferController extends BaseController {

    private final FuelTransferService fuelTransferService;

    @PostMapping
    @Operation(
        summary = "Создать внутреннее перемещение топлива",
        description = "Создает запись о внутреннем перемещении топлива между резервуарами. " +
                      "Валидирует совместимость резервуаров, соответствие типов топлива и доступную емкость.",
        requestBody = @RequestBody(
            required = true,
            description = "Данные для создания внутреннего перемещения",
            content = @Content(schema = @Schema(implementation = FuelTransferRequest.class))
        ),
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Перемещение успешно создано",
                content = @Content(schema = @Schema(implementation = FuelTransferResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Невалидные данные запроса"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав для операции"
            )
        }
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<? extends BaseResponse<?>> create(
        @Parameter(description = "Данные для создания внутреннего перемещения")
        @Valid @org.springframework.web.bind.annotation.RequestBody FuelTransferRequest request
    ) {
        return createSuccessResponse(fuelTransferService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Обновить внутреннее перемещение топлива",
        description = "Обновляет существующую запись о внутреннем перемещении топлива",
        responses = @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = FuelTransferResponse.class))
        )
    )
    public ResponseEntity<? extends BaseResponse<?>> update(
        @Parameter(description = "ID внутреннего перемещения", example = "1")
        @PathVariable Long id,
        @Parameter(description = "Данные для обновления внутреннего перемещения")
        @Valid @org.springframework.web.bind.annotation.RequestBody FuelTransferRequest request
    ) {
        return createSuccessResponse(fuelTransferService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить внутреннее перемещение по ID",
        description = "Возвращает детальную информацию о конкретном внутреннем перемещении",
        responses = @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = FuelTransferResponse.class))
        )
    )
    public ResponseEntity<? extends BaseResponse<?>> findById(
        @Parameter(description = "ID внутреннего перемещения", example = "1")
        @PathVariable Long id
    ) {
        return createSuccessResponse(fuelTransferService.findById(id));
    }

    @PostMapping("/filter")
    @Operation(
        summary = "Фильтрация и постраничное получение списка внутренних перемещений топлива",
        description = "Возвращает постраничный список внутренних перемещений с применением фильтров",
        requestBody = @RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = FuelTransferFilter.class))
        ),
        responses = @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = PageFuelTransferResponse.class))
        )
    )
    public ResponseEntity<? extends BaseResponse<?>> findAll(
        @Parameter(description = "Фильтр для поиска внутренних перемещений")
        @org.springframework.web.bind.annotation.RequestBody FuelTransferFilter filter
    ) {
        return createSuccessResponse(fuelTransferService.findAll(filter));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Удалить внутреннее перемещение",
        description = "Выполняет мягкое удаление внутреннего перемещения (устанавливает флаг deleted)",
        responses = @ApiResponse(
            responseCode = "204",
            description = "Внутреннее перемещение успешно удалено"
        )
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(
        @Parameter(description = "ID внутреннего перемещения", example = "1")
        @PathVariable Long id
    ) {
        fuelTransferService.deleteById(id);
    }
}
```

---

## Access URLs

### Development Environment

- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/api/api-docs.yaml`
- **Group-specific JSON**: `http://localhost:8080/api/api-docs/support-system`

### Test Environment

- **Swagger UI**: `https://fs-api-test.kgdigit.tech/api/swagger-ui.html`
- **OpenAPI JSON**: `https://fs-api-test.kgdigit.tech/api/api-docs`

### Production Environment

❌ **Disabled by default for security**

To enable temporarily:
```bash
export SWAGGER_ENABLED=true
export API_DOCS_ENABLED=true
```

---

## Troubleshooting

### Issue 1: Swagger UI Not Loading

**Symptoms:** 404 error on `/swagger-ui.html`

**Solutions:**
1. Check dependency is included: `springdoc-openapi-starter-webmvc-ui`
2. Verify property: `springdoc.swagger-ui.enabled=true`
3. Check context path: `/api/swagger-ui.html` not `/swagger-ui.html`
4. Ensure Spring Security allows access

### Issue 2: JWT Not Working in Swagger

**Symptoms:** 401 Unauthorized even after authorization

**Solutions:**
1. Check format: `Bearer <token>` (with space)
2. Verify token hasn't expired
3. Ensure `@SecurityRequirement(name = "bearerAuth")` on controller
4. Check global security item in `OpenApiConfig`

### Issue 3: Endpoints Not Appearing

**Symptoms:** Some controllers/methods missing from Swagger

**Solutions:**
1. Ensure controller has `@RestController` annotation
2. Check `@RequestMapping` path
3. Verify package scanning: `springdoc.packages-to-scan`
4. Check if controller is in correct API group path pattern

### Issue 4: Schema Not Showing in UI

**Symptoms:** Request/response schema shows as "object"

**Solutions:**
1. Add `@Schema` annotation to DTO class
2. Use `@Content(schema = @Schema(implementation = MyDto.class))`
3. Ensure DTOs are public with getters/setters
4. Check for circular references in DTOs

---

## Migration from Springfox

If migrating from Springfox (Swagger 2) to SpringDoc (OpenAPI 3):

### Dependency Change
```gradle
// OLD (Springfox)
implementation 'io.springfox:springfox-boot-starter:3.0.0'

// NEW (SpringDoc)
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9'
```

### Annotation Mapping

| Springfox | SpringDoc OpenAPI 3 |
|-----------|---------------------|
| `@Api` | `@Tag` |
| `@ApiOperation` | `@Operation` |
| `@ApiParam` | `@Parameter` |
| `@ApiModel` | `@Schema` |
| `@ApiModelProperty` | `@Schema` |
| `@ApiResponse` | `@ApiResponse` |
| `@ApiIgnore` | `@Hidden` |

### Configuration Migration
```java
// OLD (Springfox)
@Bean
public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.example"))
        .paths(PathSelectors.any())
        .build();
}

// NEW (SpringDoc)
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info().title("API").version("1.0"));
}
```

---

## Summary

This Swagger/OpenAPI configuration provides:

✅ **Comprehensive API Documentation** - Auto-generated from code annotations
✅ **Interactive Testing** - Swagger UI with JWT authentication support
✅ **Modular Organization** - Grouped APIs by module (Support, Oil Depot, Shared)
✅ **Production Ready** - Can be disabled for security
✅ **Developer Friendly** - Sorting, filtering, deep-linking enabled
✅ **Maintainable** - Annotations keep docs in sync with code

**Total Implementation:**
- 1 configuration class (`OpenApiConfig.java`)
- SpringDoc dependency (2.8.9)
- Configuration properties
- Controller/DTO annotations

**Access Points:**
- Development: `http://localhost:8080/api/swagger-ui.html`
- Test: `https://fs-api-test.kgdigit.tech/api/swagger-ui.html`
- Production: Disabled by default

---

**Created:** 2025-01-13
**Based on:** Fuel Ecosystem Support System v1.0
**SpringDoc Version:** 2.8.9
**OpenAPI Version:** 3.0
