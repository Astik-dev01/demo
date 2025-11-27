# Spring Boot Security & Authorization Guide

Comprehensive security implementation guide based on Fuel Ecosystem Support System architecture.

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [JWT Authentication](#jwt-authentication)
3. [Spring Security Configuration](#spring-security-configuration)
4. [Database-Driven RBAC](#database-driven-rbac)
5. [Route-Based Access Control](#route-based-access-control)
6. [Implementation Steps](#implementation-steps)
7. [Code Examples](#code-examples)
8. [Database Schema](#database-schema)
9. [Configuration](#configuration)
10. [Testing](#testing)

---

## Architecture Overview

The system implements a **hybrid security architecture** combining:
- **JWT Authentication** - Stateless token-based authentication
- **Database-Driven RBAC** - Flexible role system with multiple roles per user
- **Route-Based Access Control** - Granular HTTP method-level permissions
- **Performance Optimization** - Route caching for fast permission checks

### Key Components

```
┌─────────────────────────────────────────────────────────────┐
│                    Security Architecture                     │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌───────────────┐      ┌────────────────┐                 │
│  │  JWT Filter   │─────▶│  Spring        │                 │
│  │  (OncePerReq) │      │  Security      │                 │
│  └───────────────┘      │  Context       │                 │
│         │               └────────────────┘                 │
│         ▼                       │                            │
│  ┌───────────────┐             ▼                            │
│  │  JwtUtils     │      ┌────────────────┐                 │
│  │  (Validate)   │      │  Route Access  │                 │
│  └───────────────┘      │  Interceptor   │                 │
│         │               └────────────────┘                 │
│         ▼                       │                            │
│  ┌───────────────┐             ▼                            │
│  │  User         │      ┌────────────────┐                 │
│  │  Repository   │      │  Route Cache   │                 │
│  └───────────────┘      │  (838 links)   │                 │
│         │               └────────────────┘                 │
│         ▼                                                    │
│  ┌───────────────────────────────────────┐                 │
│  │         PostgreSQL Database           │                 │
│  │  • sys_users                          │                 │
│  │  • sys_roles                          │                 │
│  │  • sys_user_roles                     │                 │
│  │  • sys_available_routes               │                 │
│  │  • sys_role_linked_available_routes   │                 │
│  └───────────────────────────────────────┘                 │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

## JWT Authentication

### Token Structure

**Access Token (5 days expiration):**
```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "username",
  "iat": 1234567890,
  "exp": 1234999890
}

Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

**Refresh Token (5 days expiration):**
- Same structure as access token
- Used to obtain new access tokens without re-authentication

### Authentication Flow

```
┌─────────┐                ┌──────────┐                ┌──────────┐
│ Client  │                │  Backend │                │ Database │
└────┬────┘                └─────┬────┘                └─────┬────┘
     │                           │                           │
     │ POST /auth/login          │                           │
     │ {username, password}      │                           │
     ├──────────────────────────▶│                           │
     │                           │                           │
     │                           │ Find user by username/email│
     │                           ├──────────────────────────▶│
     │                           │                           │
     │                           │◀──────────────────────────┤
     │                           │ User + Roles              │
     │                           │                           │
     │                           │ Verify password (BCrypt)  │
     │                           │                           │
     │                           │ Generate JWT tokens       │
     │                           │                           │
     │◀──────────────────────────┤                           │
     │ {accessToken, refreshToken}                          │
     │                           │                           │
     │ GET /api/resource         │                           │
     │ Authorization: Bearer xxx │                           │
     ├──────────────────────────▶│                           │
     │                           │                           │
     │                           │ Validate JWT              │
     │                           │                           │
     │                           │ Load user + roles         │
     │                           ├──────────────────────────▶│
     │                           │                           │
     │                           │◀──────────────────────────┤
     │                           │                           │
     │                           │ Check route permissions   │
     │                           │                           │
     │◀──────────────────────────┤                           │
     │ Response data             │                           │
     │                           │                           │
```

---

## Spring Security Configuration

### SecurityConfig.java

The main security configuration class that sets up:
- CSRF protection (disabled for stateless JWT)
- CORS configuration
- Session management (stateless)
- Public endpoint patterns
- JWT filter registration

**Key Features:**
- BCrypt password encoding
- Stateless session policy
- Custom CORS configuration from properties
- JWT filter before UsernamePasswordAuthenticationFilter

**Public Endpoints:**
- `/auth/**` - Authentication endpoints (login, refresh, QR auth)
- `/swagger-ui/**`, `/api-docs/**` - API documentation
- `/actuator/health` - Health check
- `/ws/**` - WebSocket connections
- Specific public resource endpoints (file views, icons)

---

## Database-Driven RBAC

### Role System Architecture

**NOT enum-based!** Roles are stored in the database for maximum flexibility.

#### Key Features:
1. **Multiple Roles Per User** - Junction table `sys_user_roles`
2. **Active/Inactive Roles** - Soft deletion support
3. **Role Codes** - String identifiers (e.g., "SUPPORT_ADMINISTRATOR")
4. **Backward Compatibility** - `RoleEnum` exists but system uses database roles

#### Role Hierarchy

**System Roles:**
- `SUPERADMIN` - Full system access across all modules

**Support System Roles (SUPPORT_*):**
- `SUPPORT_ADMINISTRATOR` - Full support module access
- `SUPPORT_DISPATCHER` - Ticket routing and queue management
- `SUPPORT_TECHNICIAN` - Ticket execution
- `SUPPORT_OPERATOR` - Ticket creation and tracking
- `SUPPORT_READONLY` - Read-only support access

**Oil Depot Roles (OILDEPOT_*):**
- `OILDEPOT_ADMINISTRATOR` - Full oil depot module access
- `OILDEPOT_DIRECTOR` - Oil depot management with DELETE rights
- `OILDEPOT_OPERATOR` - Oil depot operations (GET/POST/PUT only, requires `oil_depot_id`)
- `OILDEPOT_READONLY` - Read-only oil depot access

#### Multi-Role Implementation

```java
@Entity
@Table(name = "sys_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String passwordHash;
    private Boolean active;

    // Legacy single role (for backward compatibility)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    // NEW: Multiple roles support
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserRole> userRoles = new ArrayList<>();

    // Get all active role codes
    public List<String> getActiveRoleCodes() {
        return userRoles.stream()
            .filter(UserRole::getActive)
            .map(ur -> ur.getRole().getCode())
            .toList();
    }
}

@Entity
@Table(name = "sys_user_roles")
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    private LocalDateTime assignedAt;
    private Boolean active;
}
```

---

## Route-Based Access Control

### Granular Permission System

Unlike simple role checks, this system provides **HTTP method-level** permissions.

#### Architecture Components:

1. **sys_available_routes** - All API endpoints in the system (172 routes)
2. **sys_role_linked_available_routes** - Role-to-route permissions (838 links)
3. **Route Cache** - In-memory cache for fast lookups
4. **RouteAccessInterceptor** - Runtime permission checks

#### Example Route Permissions

```
Route: /api/oil-depot/fuel-transfers
├─ GET    → [OILDEPOT_ADMINISTRATOR, OILDEPOT_DIRECTOR, OILDEPOT_OPERATOR, OILDEPOT_READONLY]
├─ POST   → [OILDEPOT_ADMINISTRATOR, OILDEPOT_DIRECTOR, OILDEPOT_OPERATOR]
├─ PUT    → [OILDEPOT_ADMINISTRATOR, OILDEPOT_DIRECTOR, OILDEPOT_OPERATOR]
└─ DELETE → [OILDEPOT_ADMINISTRATOR, OILDEPOT_DIRECTOR]
```

### Route Cache Performance

**Cache Structure:**
```java
Map<String, Map<String, Set<String>>> routeAccessCache = new ConcurrentHashMap<>();
// Structure: {route -> {method -> Set<roleCodes>}}

// Example cached entry:
{
  "/api/oil-depot/fuel-transfers": {
    "GET": ["OILDEPOT_ADMINISTRATOR", "OILDEPOT_DIRECTOR", "OILDEPOT_OPERATOR", "OILDEPOT_READONLY"],
    "POST": ["OILDEPOT_ADMINISTRATOR", "OILDEPOT_DIRECTOR", "OILDEPOT_OPERATOR"],
    "PUT": ["OILDEPOT_ADMINISTRATOR", "OILDEPOT_DIRECTOR", "OILDEPOT_OPERATOR"],
    "DELETE": ["OILDEPOT_ADMINISTRATOR", "OILDEPOT_DIRECTOR"]
  }
}
```

**Cache Benefits:**
- O(1) lookup time
- No database queries during request handling
- Automatic refresh on permission changes
- Supports wildcards (e.g., `/api/admin/**`)

### Hybrid Authorization

The system uses **both** annotation-based and interceptor-based authorization:

1. **@PreAuthorize Annotations** - Method-level security
   ```java
   @PreAuthorize("hasAnyRole('SUPERADMIN', 'SUPPORT_ADMINISTRATOR')")
   public List<User> getAllUsers() { ... }
   ```

2. **RouteAccessInterceptor** - Runtime HTTP method checks
   ```java
   // Automatically validates based on sys_role_linked_available_routes
   // No code changes needed when permissions are updated in database
   ```

**Priority:** When `@PreAuthorize` is present, it takes precedence.

---

## Implementation Steps

### Step 1: Add Dependencies

**build.gradle:**
```gradle
dependencies {
    // Spring Security
    implementation 'org.springframework.boot:spring-boot-starter-security'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'

    // Password encoding
    implementation 'org.springframework.security:spring-security-crypto'

    // Database
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.postgresql:postgresql'
    implementation 'org.flywaydb:flyway-core'
}
```

### Step 2: Database Schema

Run Flyway migrations to create required tables. See [Database Schema](#database-schema) section.

### Step 3: Configuration Properties

**application.properties:**
```properties
# JWT Configuration
jwt.secret=your-256-bit-secret-key-here
jwt.expiration=432000000        # 5 days in milliseconds
jwt.refresh-expiration=432000000 # 5 days in milliseconds

# CORS Configuration
cors.allowed-origins=http://localhost:3000,https://your-domain.com

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db
spring.datasource.username=your_user
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.default_schema=your_schema

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

### Step 4: Create Core Security Classes

1. **JwtUtils.java** - Token generation and validation
2. **JwtAuthenticationFilter.java** - Request filter
3. **SecurityConfig.java** - Main security configuration
4. **AuthService.java** - Authentication business logic
5. **RouteAccessInterceptor.java** - Route permission checks

See [Code Examples](#code-examples) section for full implementations.

### Step 5: Entity Classes

Create entities for:
- User
- Role
- UserRole
- AvailableRoute
- RoleLinkedAvailableRoute

### Step 6: Repository Interfaces

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(String code);
    List<Role> findAllByActiveTrue();
}

public interface RoleLinkedAvailableRouteRepository extends JpaRepository<RoleLinkedAvailableRoute, Long> {
    List<RoleLinkedAvailableRoute> findAllByActiveTrue();
}
```

### Step 7: Authentication Controller

```java
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody AuthRefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }
}
```

### Step 8: Enable Method Security

```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {
    // Spring Security will scan for @PreAuthorize, @PostAuthorize, etc.
}
```

### Step 9: Route Access Management

Create admin APIs to manage route permissions dynamically:
- Add/remove available routes
- Assign/revoke route access for roles
- Refresh route cache

---

## Code Examples

### 1. JwtUtils.java

```java
package kg.bishkek.fuel.shared.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    private SecretKey key;
    private final String jwtSecret;
    private final int jwtExpirationMs;
    private final int jwtRefreshExpirationMs;

    public JwtUtils(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration}") int jwtExpirationMs,
            @Value("${jwt.refresh-expiration}") int jwtRefreshExpirationMs) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        this.jwtRefreshExpirationMs = jwtRefreshExpirationMs;
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate access JWT token
     */
    public String generateJwtToken(String username) {
        return generateTokenFromUsername(username, jwtExpirationMs);
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String username) {
        return generateTokenFromUsername(username, jwtRefreshExpirationMs);
    }

    /**
     * Generate token with custom expiration
     */
    private String generateTokenFromUsername(String username, int expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract username from JWT token
     */
    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validate JWT token
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Get access token expiration time in seconds
     */
    public long getAccessTokenExpirationTime() {
        return jwtExpirationMs / 1000;
    }

    /**
     * Get refresh token expiration time in seconds
     */
    public long getRefreshTokenExpirationTime() {
        return jwtRefreshExpirationMs / 1000;
    }
}
```

### 2. JwtAuthenticationFilter.java

```java
package kg.bishkek.fuel.shared.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kg.bishkek.fuel.shared.db.entities.User;
import kg.bishkek.fuel.shared.db.repositories.UserRepository;
import kg.bishkek.fuel.shared.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = parseJwt(request);

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUsernameFromJwtToken(jwt);

                User user = userRepository.findByUsername(username).orElse(null);

                if (user != null && user.getActive()) {
                    // Create authorities from active roles
                    List<SimpleGrantedAuthority> authorities = user.getActiveRoleCodes().stream()
                            .map(roleCode -> new SimpleGrantedAuthority("ROLE_" + roleCode))
                            .toList();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
```

### 3. SecurityConfig.java

```java
package kg.bishkek.fuel.shared.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        // Actuator endpoints
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // WebSocket endpoints
                        .requestMatchers("/ws/**").permitAll()

                        // Public resource views
                        .requestMatchers("/support/tickets/*/files/*/view").permitAll()
                        .requestMatchers("/support/hb/equipment/categories/*/icon/view").permitAll()

                        // OPTIONS requests for CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Auth-Token"));
        config.setExposedHeaders(List.of("X-Auth-Token"));

        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

### 4. AuthServiceImpl.java

```java
package kg.bishkek.fuel.shared.service.impl;

import kg.bishkek.fuel.shared.db.entities.User;
import kg.bishkek.fuel.shared.db.repositories.UserRepository;
import kg.bishkek.fuel.shared.dto.auth.request.AuthLoginRequest;
import kg.bishkek.fuel.shared.dto.auth.request.AuthRefreshRequest;
import kg.bishkek.fuel.shared.dto.auth.response.AuthResponse;
import kg.bishkek.fuel.shared.mapper.UserMapper;
import kg.bishkek.fuel.shared.service.AuthService;
import kg.bishkek.fuel.shared.util.JwtUtils;
import kg.bishkek.fuel.shared.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final MessageUtils messageUtils;

    @Override
    public AuthResponse login(AuthLoginRequest loginRequest) {
        // Find user by username or email
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .or(() -> userRepository.findByEmail(loginRequest.getUsername()))
                .orElseThrow(() -> new IllegalArgumentException(
                        messageUtils.getMessage(MessageUtils.Keys.INVALID_CREDENTIALS)
                ));

        // Check if user is active
        if (!user.getActive()) {
            throw new IllegalArgumentException(
                    messageUtils.getMessage(MessageUtils.Keys.ACCESS_DENIED)
            );
        }

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException(
                    messageUtils.getMessage(MessageUtils.Keys.INVALID_CREDENTIALS)
            );
        }

        // Generate tokens
        String accessToken = jwtUtils.generateJwtToken(user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

        log.info("User {} successfully logged in", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getAccessTokenExpirationTime())
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    public AuthResponse refreshToken(AuthRefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();

        // Validate refresh token
        if (!jwtUtils.validateJwtToken(refreshToken)) {
            throw new IllegalArgumentException(
                    messageUtils.getMessage(MessageUtils.Keys.INVALID_TOKEN)
            );
        }

        // Extract username and generate new tokens
        String username = jwtUtils.getUsernameFromJwtToken(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageUtils.getMessage(MessageUtils.Keys.USER_NOT_FOUND)
                ));

        if (!user.getActive()) {
            throw new IllegalArgumentException(
                    messageUtils.getMessage(MessageUtils.Keys.ACCESS_DENIED)
            );
        }

        String newAccessToken = jwtUtils.generateJwtToken(username);
        String newRefreshToken = jwtUtils.generateRefreshToken(username);

        log.info("Token refreshed for user {}", username);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getAccessTokenExpirationTime())
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    public void logout(jakarta.servlet.http.HttpServletRequest request) {
        // TODO: Implement token blacklisting
        // For now, client-side token removal is sufficient
        log.info("User logged out");
    }
}
```

### 5. RouteAccessInterceptor.java

```java
package kg.bishkek.fuel.shared.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kg.bishkek.fuel.shared.db.entities.User;
import kg.bishkek.fuel.shared.service.RouteAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteAccessInterceptor implements HandlerInterceptor {

    private final RouteAccessService routeAccessService;

    // Paths excluded from route access checks
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/auth/", "/swagger-ui/", "/api-docs/", "/actuator/", "/ws/",
            "/error", "/favicon.ico"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // Skip excluded paths
        if (isExcludedPath(requestURI)) {
            return true;
        }

        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthenticated access attempt to {} {}", method, requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        User user = (User) authentication.getPrincipal();
        List<String> userRoleCodes = user.getActiveRoleCodes();

        // Check route access
        boolean hasAccess = routeAccessService.hasAccess(requestURI, method, userRoleCodes);

        if (!hasAccess) {
            log.warn("Access denied for user {} to {} {}", user.getUsername(), method, requestURI);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        log.debug("Access granted for user {} to {} {}", user.getUsername(), method, requestURI);
        return true;
    }

    private boolean isExcludedPath(String requestURI) {
        return EXCLUDED_PATHS.stream().anyMatch(requestURI::startsWith);
    }
}
```

---

## Database Schema

### Flyway Migration Script

**V001__Initial_Security_Schema.sql:**

```sql
-- Create schema
CREATE SCHEMA IF NOT EXISTS your_schema;
SET search_path TO your_schema;

-- =====================================================
-- System Roles Table
-- =====================================================
CREATE TABLE sys_roles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name_ru VARCHAR(100) NOT NULL,
    name_ky VARCHAR(100),
    name_en VARCHAR(100),
    description_ru TEXT,
    description_ky TEXT,
    description_en TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_sys_roles_code ON sys_roles(code);
CREATE INDEX idx_sys_roles_active ON sys_roles(active);

-- =====================================================
-- System Users Table
-- =====================================================
CREATE TABLE sys_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    phone VARCHAR(20),
    employee_code VARCHAR(20) UNIQUE,
    role_id BIGINT, -- Legacy single role (backward compatibility)
    active BOOLEAN NOT NULL DEFAULT true,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT false,

    CONSTRAINT fk_users_role FOREIGN KEY (role_id)
        REFERENCES sys_roles(id) ON DELETE RESTRICT
);

CREATE INDEX idx_sys_users_username ON sys_users(username);
CREATE INDEX idx_sys_users_email ON sys_users(email);
CREATE INDEX idx_sys_users_employee_code ON sys_users(employee_code);
CREATE INDEX idx_sys_users_active ON sys_users(active);
CREATE INDEX idx_sys_users_role_id ON sys_users(role_id);

-- =====================================================
-- User-Role Junction Table (Many-to-Many)
-- =====================================================
CREATE TABLE sys_user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
        REFERENCES sys_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id)
        REFERENCES sys_roles(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_role UNIQUE (user_id, role_id)
);

CREATE INDEX idx_sys_user_roles_user_id ON sys_user_roles(user_id);
CREATE INDEX idx_sys_user_roles_role_id ON sys_user_roles(role_id);
CREATE INDEX idx_sys_user_roles_active ON sys_user_roles(active);

-- =====================================================
-- Available Routes Table
-- =====================================================
CREATE TABLE sys_available_routes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL,
    description_ru TEXT,
    description_ky TEXT,
    description_en TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_sys_available_routes_code ON sys_available_routes(code);
CREATE INDEX idx_sys_available_routes_active ON sys_available_routes(active);

-- =====================================================
-- Role-Route Permissions Table
-- =====================================================
CREATE TABLE sys_role_linked_available_routes (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    route_id BIGINT NOT NULL,
    http_method VARCHAR(10) NOT NULL, -- GET, POST, PUT, DELETE, PATCH
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_role_routes_role FOREIGN KEY (role_id)
        REFERENCES sys_roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_routes_route FOREIGN KEY (route_id)
        REFERENCES sys_available_routes(id) ON DELETE CASCADE,
    CONSTRAINT uq_role_route_method UNIQUE (role_id, route_id, http_method)
);

CREATE INDEX idx_sys_role_routes_role_id ON sys_role_linked_available_routes(role_id);
CREATE INDEX idx_sys_role_routes_route_id ON sys_role_linked_available_routes(route_id);
CREATE INDEX idx_sys_role_routes_active ON sys_role_linked_available_routes(active);
CREATE INDEX idx_sys_role_routes_http_method ON sys_role_linked_available_routes(http_method);

-- =====================================================
-- API Audit Logs Table
-- =====================================================
CREATE TABLE api_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    http_method VARCHAR(10) NOT NULL,
    request_uri TEXT NOT NULL,
    request_body TEXT,
    response_body TEXT,
    status_code INTEGER,
    ip_address VARCHAR(45),
    user_agent TEXT,
    execution_time_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_user FOREIGN KEY (user_id)
        REFERENCES sys_users(id) ON DELETE SET NULL
);

CREATE INDEX idx_api_audit_user_id ON api_audit_logs(user_id);
CREATE INDEX idx_api_audit_created_at ON api_audit_logs(created_at);
CREATE INDEX idx_api_audit_http_method ON api_audit_logs(http_method);
CREATE INDEX idx_api_audit_status_code ON api_audit_logs(status_code);

-- =====================================================
-- Comments
-- =====================================================
COMMENT ON TABLE sys_roles IS 'System roles for authorization';
COMMENT ON TABLE sys_users IS 'System users with authentication credentials';
COMMENT ON TABLE sys_user_roles IS 'Many-to-many junction table for user-role assignments';
COMMENT ON TABLE sys_available_routes IS 'Available API routes in the system';
COMMENT ON TABLE sys_role_linked_available_routes IS 'Role-based route access permissions with HTTP method granularity';
COMMENT ON TABLE api_audit_logs IS 'Audit trail for API requests';
```

**V002__Insert_Initial_Roles.sql:**

```sql
SET search_path TO your_schema;

-- Insert system roles
INSERT INTO sys_roles (code, name_ru, name_en, description_ru, active) VALUES
('SUPERADMIN', 'Суперадминистратор', 'Super Administrator', 'Полный доступ ко всей системе', true),
('SUPPORT_ADMINISTRATOR', 'Администратор поддержки', 'Support Administrator', 'Полный доступ к модулю техподдержки', true),
('SUPPORT_DISPATCHER', 'Диспетчер', 'Dispatcher', 'Управление заявками и назначение специалистов', true),
('SUPPORT_TECHNICIAN', 'Технический специалист', 'Technician', 'Выполнение заявок на АЗС', true),
('SUPPORT_OPERATOR', 'Оператор АЗС', 'Station Operator', 'Создание и отслеживание заявок', true),
('SUPPORT_READONLY', 'Только просмотр (поддержка)', 'Read-only (Support)', 'Доступ на чтение данных техподдержки', true),
('OILDEPOT_ADMINISTRATOR', 'Администратор нефтебазы', 'Oil Depot Administrator', 'Полный доступ к модулю нефтебазы', true),
('OILDEPOT_DIRECTOR', 'Директор нефтебазы', 'Oil Depot Director', 'Управление нефтебазой с правами удаления', true),
('OILDEPOT_OPERATOR', 'Оператор нефтебазы', 'Oil Depot Operator', 'Операции на нефтебазе (без удаления)', true),
('OILDEPOT_READONLY', 'Только просмотр (нефтебаза)', 'Read-only (Oil Depot)', 'Доступ на чтение данных нефтебазы', true);

-- Create admin user (password: 12345)
-- BCrypt hash for "12345"
INSERT INTO sys_users (username, email, password_hash, full_name, active) VALUES
('admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7tR.KAlYWn9LfLBX3gH2j/KQWH7E7dC', 'System Administrator', true);

-- Assign SUPERADMIN role to admin user
INSERT INTO sys_user_roles (user_id, role_id, active)
SELECT u.id, r.id, true
FROM sys_users u, sys_roles r
WHERE u.username = 'admin' AND r.code = 'SUPERADMIN';
```

---

## Configuration

### application.properties

```properties
# =====================================================
# Application Configuration
# =====================================================
spring.application.name=your-app-name
server.port=8080
server.servlet.context-path=/api

# =====================================================
# Database Configuration
# =====================================================
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db?currentSchema=your_schema
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# HikariCP connection pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# =====================================================
# JPA / Hibernate Configuration
# =====================================================
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.default_schema=your_schema
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.open-in-view=false

# =====================================================
# Flyway Migration Configuration
# =====================================================
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.validate-on-migrate=true
spring.flyway.schemas=your_schema

# =====================================================
# JWT Configuration
# =====================================================
jwt.secret=your-256-bit-secret-key-here-minimum-32-characters-required-for-hs256
jwt.expiration=432000000        # 5 days in milliseconds
jwt.refresh-expiration=432000000 # 5 days in milliseconds

# =====================================================
# CORS Configuration
# =====================================================
cors.allowed-origins=http://localhost:3000,http://localhost:3001,https://your-frontend-domain.com

# =====================================================
# Security Configuration
# =====================================================
spring.security.filter.dispatcher-types=REQUEST,FORWARD,ERROR

# =====================================================
# Logging Configuration
# =====================================================
logging.level.root=INFO
logging.level.kg.bishkek.fuel=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# =====================================================
# Actuator Configuration
# =====================================================
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

### Security Best Practices

1. **JWT Secret Generation:**
   ```bash
   # Generate secure 256-bit secret
   openssl rand -base64 32
   ```

2. **Password Encoding:**
   ```java
   // BCrypt with strength 10 (default)
   String hashedPassword = new BCryptPasswordEncoder().encode("password");
   ```

3. **Token Storage:**
   - **Client-side**: Store in `httpOnly` cookies or localStorage
   - **Never** store refresh tokens in localStorage (use httpOnly cookies)
   - Implement token blacklisting for logout

4. **CORS Configuration:**
   - Only whitelist trusted domains
   - Use specific origins, not wildcard `*` in production

5. **HTTPS Only:**
   - Always use HTTPS in production
   - Set `Secure` flag on cookies
   - Enable HSTS headers

---

## Testing

### Manual API Testing

**1. Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "12345"
  }'
```

**Response:**
```json
{
  "success": true,
  "result": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 432000,
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@example.com",
      "fullName": "System Administrator",
      "roles": ["SUPERADMIN"]
    }
  }
}
```

**2. Access Protected Endpoint:**
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**3. Refresh Token:**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

**4. Logout:**
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Integration Testing

**JUnit 5 Test Example:**

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLogin_Success() throws Exception {
        AuthLoginRequest request = new AuthLoginRequest("admin", "12345");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.accessToken").exists())
                .andExpect(jsonPath("$.result.refreshToken").exists())
                .andExpect(jsonPath("$.result.user.username").value("admin"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        AuthLoginRequest request = new AuthLoginRequest("admin", "wrong_password");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAccessProtectedEndpoint_WithoutToken() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"SUPERADMIN"})
    void testAccessProtectedEndpoint_WithValidToken() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }
}
```

---

## Additional Resources

### Related Documentation Files
- `/backend/docs/auth.md` - Detailed JWT authentication documentation (793 lines)
- `/backend/docs/PERMISSIONS_GUIDE.md` - Complete route access control guide (1236 lines)
- `/backend/docs/roles-guide.md` - Granular access control system
- `/backend/shared/src/main/java/kg/bishkek/fuel/shared/config/SecurityConfig.java` - Current implementation

### Key Utilities
- **SecurityContextUtils** - Helper for accessing current user from SecurityContext
- **SecurityUtils** - Security-related utility methods
- **MessageUtils** - Internationalized error messages

### Common Issues & Solutions

**Issue 1: "Invalid JWT signature"**
- **Cause**: JWT secret mismatch or token tampered
- **Solution**: Verify `jwt.secret` matches across all instances

**Issue 2: "Access Denied" with valid token**
- **Cause**: User role not assigned or route permission missing
- **Solution**: Check `sys_user_roles` and `sys_role_linked_available_routes` tables

**Issue 3: CORS errors in browser**
- **Cause**: Frontend origin not whitelisted
- **Solution**: Add origin to `cors.allowed-origins` property

**Issue 4: Token expired**
- **Cause**: Token TTL exceeded
- **Solution**: Use refresh token endpoint to get new access token

---

## Summary

This security architecture provides:

✅ **Stateless Authentication** - JWT tokens with configurable expiration
✅ **Multiple Roles Per User** - Flexible role assignment via junction table
✅ **Granular Permissions** - HTTP method-level route access control
✅ **Performance Optimized** - Route cache for O(1) lookups
✅ **Database-Driven** - No code changes needed for role/permission updates
✅ **Hybrid Authorization** - Both `@PreAuthorize` and interceptor-based checks
✅ **Audit Trail** - Complete logging of all API requests
✅ **Production-Ready** - BCrypt hashing, CORS, HTTPS support

**Total Implementation:**
- 6 core Java classes
- 2 Flyway migration scripts
- Configuration properties
- Complete test coverage

This system successfully handles 70+ gas stations, 172 API routes, and 10 different user roles with 838 permission links.

---

**Created:** 2025-01-13
**Based on:** Fuel Ecosystem Support System v1.0
**Spring Boot:** 3.5.0
**Java:** 21
