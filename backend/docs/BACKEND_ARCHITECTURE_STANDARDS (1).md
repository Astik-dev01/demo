# Backend Architecture Standardization Guide for Claude Code

Данный документ содержит **готовые шаблоны кода** и **пошаговые инструкции** для стандартизации backend проектов. Используйте этот документ для переделки существующих проектов под единый архитектурный стандарт.

## 🎯 Цель документа

Этот гайд поможет Claude Code автоматически переделать любой Spring Boot проект под стандартизированную архитектуру, включающую:
- Единообразные модели ответов JSON
- Стандартную пагинацию и фильтрацию
- Централизованную обработку ошибок
- Систему локализации
- MapStruct мапперы
- JPA Specifications

## 🏗️ Технологический стек

### Обязательная основа
- **Java**: 21
- **Spring Boot**: 3.5.0
- **База данных**: PostgreSQL
- **Сборщик**: Gradle

### Ключевые зависимости (без версий)
```gradle
// Spring Boot Core
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-validation'
implementation 'org.springframework.boot:spring-boot-starter-actuator'

// Database
runtimeOnly 'org.postgresql:postgresql'
implementation 'org.flywaydb:flyway-core'

// Documentation
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui'

// Utilities
implementation 'org.apache.commons:commons-lang3'
implementation 'com.fasterxml.jackson.core:jackson-databind'
implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310'

// Mapping & Annotations
implementation 'org.mapstruct:mapstruct'
annotationProcessor 'org.mapstruct:mapstruct-processor'
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'

// Testing
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.testcontainers:junit-jupiter'
testImplementation 'org.testcontainers:postgresql'
```

## 📁 Структура проекта

### Стандартная иерархия пакетов
```
src/main/java/kg/bank/{project-name}/
├── config/                          # Spring конфигурация
├── controller/                      # REST endpoints
├── service/                        # Бизнес-логика
│   └── impl/                       # Реализации сервисов
├── dto/                            # Data Transfer Objects
│   ├── {entity}/                   # DTO по доменам  
│   │   ├── request/                # Request models
│   │   ├── response/               # Response models
│   │   └── filter/                 # Filter models
│   └── validation/                 # Validation errors
├── db/
│   ├── entity/                     # JPA entities
│   ├── enums/                      # Enumerations
│   └── repository/                 # Data access
│       └── specifications/         # JPA Specifications
├── mapper/                         # MapStruct mappers
├── exception/                      # Custom exceptions
│   └── handler/                    # Global handlers
├── util/                           # Utilities
└── validation/                     # Custom validators
```

## 🏛️ Базовые классы (готовые шаблоны)

### 1. BasePageRequest.java
```java
package kg.bank.{project}.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class BasePageRequest {
    @Schema(description = "Номер страницы", example = "1")
    @Builder.Default
    Integer page = 1;

    @Schema(description = "Размер страницы", example = "15")
    @Builder.Default
    Integer size = 15;
}
```

### 2. BasePageResponse.java
```java
package kg.bank.{project}.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BasePageResponse {
    @Schema(description = "Текущая страница", example = "1")
    private int page;

    @Schema(description = "Размер страницы", example = "15")
    private int size;

    @Schema(description = "Общее количество элементов", example = "100")
    private long totalElements;

    @Schema(description = "Общее количество страниц", example = "7")
    private int totalPages;
}
```

### 3. BaseResponse.java
```java
package kg.bank.{project}.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "Общий класс для ответов")
public class BaseResponse {
    @Schema(description = "Статус выполнения запроса", example = "true")
    private boolean success;
    
    @Schema(description = "Сообщение об ошибке", example = "Ошибка")
    private Object msg;
    
    @Schema(description = "Результат выполнения запроса", example = "Объект")
    private Object res;
    
    @Builder.Default
    @Schema(description = "Время выполнения запроса", example = "2025-01-01 00:00:00")
    private String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    
    @Builder.Default
    @Schema(description = "Версия сервиса", example = "1.0.0")
    private String ver = "1.0.0";
}
```

### 4. BaseController.java
```java
package kg.bank.{project}.controller;

import jakarta.servlet.http.HttpServletRequest;
import kg.bank.{project}.dto.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Базовый контроллер для всех REST endpoints
 */
public class BaseController {

    /**
     * Константы для стандартизации ответов
     */
    public static class Constants {
        public static final boolean SUCCESS = true;
        public static final boolean ERROR = false;
        public static final String UNDEFINED_FIELD = "";

        public enum RestResponseMessages {
            BAD_REQUEST
        }

        public enum LOG_STATUS {
            ERROR, SUCCESS
        }
    }

    /**
     * Определяет реальный IP адрес клиента
     */
    public static String tryDetectRemoteClientIp(HttpServletRequest httpServletRequest) {
        if (httpServletRequest != null) {
            try {
                String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
                return xForwardedFor != null ? xForwardedFor : httpServletRequest.getRemoteAddr();
            } catch (Exception ignored) {
            }
            return httpServletRequest.getRemoteAddr();
        }
        return null;
    }

    /**
     * Извлекает User-Agent из заголовков
     */
    public static String getUserAgent(HttpServletRequest httpServletRequest) {
        try {
            return httpServletRequest != null ? httpServletRequest.getHeader("User-Agent") : null;
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Преобразует номер страницы из 1-based в 0-based
     */
    public static Integer getPage(Integer page) {
        if (page != null && page > 0) {
            return page - 1;
        }
        return 0;
    }

    /**
     * Создает успешный ответ с данными
     */
    protected ResponseEntity<BaseResponse> createSuccessResponse(Object data) {
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .success(Constants.SUCCESS)
                        .msg(null)
                        .res(data)
                        .build()
        );
    }

    /**
     * Создает успешный ответ с кастомным статусом
     */
    protected ResponseEntity<BaseResponse> createSuccessResponse(Object data, HttpStatus status) {
        return ResponseEntity.status(status).body(
                BaseResponse.builder()
                        .success(Constants.SUCCESS)
                        .msg(null)
                        .res(data)
                        .build()
        );
    }

    /**
     * Создает ответ с ошибкой
     */
    protected ResponseEntity<BaseResponse> createErrorResponse(Object message) {
        return ResponseEntity.badRequest().body(
                BaseResponse.builder()
                        .success(Constants.ERROR)
                        .msg(message)
                        .res(null)
                        .build()
        );
    }

    /**
     * Создает ответ с ошибкой и кастомным статусом
     */
    protected ResponseEntity<BaseResponse> createErrorResponse(Object message, HttpStatus status) {
        return ResponseEntity.status(status).body(
                BaseResponse.builder()
                        .success(Constants.ERROR)
                        .msg(message)
                        .res(null)
                        .build()
        );
    }
}
```

## 📊 Пагинация и фильтрация

### 1. Page{Entity}Response шаблон
```java
package kg.bank.{project}.dto.{entity}.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import kg.bank.{project}.dto.BasePageResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Ответ постраничный {entity}")
public class Page{Entity}Response extends BasePageResponse {
    @ArraySchema(schema = @Schema(description = "Список {entity}", implementation = {Entity}Response.class))
    List<{Entity}Response> content;
}
```

### 2. Filter DTO шаблон
```java
package kg.bank.{project}.dto.{entity}.filter;

import kg.bank.{project}.dto.BasePageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class {Entity}Filter extends BasePageRequest {
    
    // Основные поля фильтрации
    private String name;
    private Long companyId;
    private LocalDate createdAtFrom;
    private LocalDate createdAtTo;
    
    // Булевые фильтры
    private Boolean activeOnly;
    private Boolean deletedOnly;
    
    // Enum фильтры
    private StatusEnum status;
}
```

### 3. JPA Specifications шаблон
```java
package kg.bank.{project}.db.repository.specifications;

import jakarta.persistence.criteria.Predicate;
import kg.bank.{project}.db.entity.{Entity};
import kg.bank.{project}.dto.{entity}.filter.{Entity}Filter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class {Entity}Specifications {

    public static Specification<{Entity}> withFilter({Entity}Filter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Базовая фильтрация по удаленным записям (ОБЯЗАТЕЛЬНО)
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));

            // Фильтрация по названию
            if (filter.getName() != null && !filter.getName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"
                ));
            }

            // Фильтрация по компании (если применимо)
            if (filter.getCompanyId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("company").get("id"), filter.getCompanyId()));
            }

            // Фильтрация по дате создания
            if (filter.getCreatedAtFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        criteriaBuilder.function("DATE", LocalDate.class, root.get("createdAt")),
                        filter.getCreatedAtFrom()
                ));
            }
            if (filter.getCreatedAtTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        criteriaBuilder.function("DATE", LocalDate.class, root.get("createdAt")),
                        filter.getCreatedAtTo()
                ));
            }

            // Булевые фильтры
            if (Boolean.TRUE.equals(filter.getActiveOnly())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), StatusEnum.ACTIVE));
            }

            // Enum фильтры
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

### 4. Контроллер с пагинацией (пример)
```java
@PostMapping("/filter")
@Operation(summary = "Фильтрация {entity} с пагинацией")
public ResponseEntity<BaseResponse> filter(@RequestBody {Entity}Filter filter) {
    
    // Преобразование page в 0-based
    Pageable pageable = PageRequest.of(
            BaseController.getPage(filter.getPage()),
            filter.getSize()
    );
    
    // Применение фильтра через Specification
    Specification<{Entity}> spec = {Entity}Specifications.withFilter(filter);
    Page<{Entity}> page = {entity}Repository.findAll(spec, pageable);
    
    // Маппинг в DTO
    Page{Entity}Response response = {entity}Mapper.toPageResponse(page);
    
    return createSuccessResponse(response);
}
```

## 🔄 MapStruct мапперы

### 1. Стандартный шаблон маппера
```java
package kg.bank.{project}.mapper;

import kg.bank.{project}.db.entity.{Entity};
import kg.bank.{project}.dto.{entity}.request.{Entity}Request;
import kg.bank.{project}.dto.{entity}.response.{Entity}Response;
import kg.bank.{project}.dto.{entity}.response.Page{Entity}Response;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования {Entity} сущностей в DTO и обратно
 */
@Component
public class {Entity}Mapper {

    /**
     * Преобразование Entity в Response
     */
    public {Entity}Response toResponse({Entity} entity) {
        if (entity == null) {
            return null;
        }

        return {Entity}Response.builder()
                .id(entity.getId())
                .name(entity.getName())
                // ... другие поля
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Преобразование Request в Entity
     */
    public {Entity} toEntity({Entity}Request request) {
        if (request == null) {
            return null;
        }

        {Entity} entity = new {Entity}();
        entity.setName(request.getName());
        // ... другие поля
        entity.setDeleted(false);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    /**
     * Обновление существующей сущности из Request
     */
    public void updateEntityFromRequest({Entity}Request request, {Entity} entity) {
        if (request == null || entity == null) {
            return;
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        // ... другие поля
        entity.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Преобразование списка Entity в список Response
     */
    public List<{Entity}Response> toResponseList(List<{Entity}> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Преобразование Page<Entity> в Page{Entity}Response
     */
    public Page{Entity}Response toPageResponse(Page<{Entity}> page) {
        if (page == null) {
            return null;
        }

        return Page{Entity}Response.builder()
                .content(toResponseList(page.getContent()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
```

### 2. Ключевые принципы мапперов

**ВАЖНО: Используйте @Component вместо @Mapper**
```java
@Component  // ✅ Правильно
public class EntityMapper {
    // реализация
}

@Mapper     // ❌ Не используйте
public interface EntityMapper {
    // MapStruct автогенерация
}
```

**Обязательные методы в каждом маппере:**
- `toResponse(Entity)` - entity → response DTO
- `toEntity(Request)` - request DTO → entity
- `updateEntityFromRequest(Request, Entity)` - обновление entity
- `toResponseList(List<Entity>)` - список entities → список response DTO
- `toPageResponse(Page<Entity>)` - страница entities → page response DTO

**Стандартные поля для всех entities:**
```java
// Аудит поля (всегда включать)
.createdAt(entity.getCreatedAt())
.updatedAt(entity.getUpdatedAt())

// При создании новой entity
entity.setDeleted(false);
entity.setCreatedAt(LocalDateTime.now());

// При обновлении entity
entity.setUpdatedAt(LocalDateTime.now());
```

## 🌐 Система локализации

### 1. MessageUtils.java
```java
package kg.bank.{project}.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;

/**
 * Утилитный класс для работы с локализованными сообщениями
 */
@Component
@RequiredArgsConstructor
public class MessageUtils {

    private final MessageSource messageSource;

    /**
     * Получает локализованное сообщение по ключу на основе Accept-Language заголовка
     */
    public String getMessage(String key) {
        return getMessageFromAcceptLanguage(key, (Object) null);
    }

    /**
     * Получает локализованное сообщение с параметрами
     */
    public String getMessage(String key, Object... args) {
        return getMessageFromAcceptLanguage(key, args);
    }

    /**
     * Получает локализованное сообщение на основе заголовка Accept-Language
     */
    public String getMessageFromAcceptLanguage(String key, Object... args) {
        Locale locale = getLocaleFromAcceptLanguageHeader();
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (org.springframework.context.NoSuchMessageException e) {
            // Если сообщение не найдено, возвращаем сам ключ
            return key;
        }
    }

    /**
     * Извлекает локаль из заголовка Accept-Language текущего HTTP запроса
     */
    public static Locale getLocaleFromAcceptLanguageHeader() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                String acceptLanguage = request.getHeader("Accept-Language");

                if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
                    String firstLanguage = acceptLanguage.split(",")[0].trim();
                    String languageCode = firstLanguage.split(";")[0].trim();

                    // Поддерживаем только ru, ky, en
                    return switch (languageCode.toLowerCase()) {
                        case "ru", "ru-ru" -> Locale.of("ru");
                        case "ky", "ky-kg" -> Locale.of("ky");
                        case "en", "en-us" -> Locale.of("en");
                        default -> Locale.of("ru"); // Русский по умолчанию
                    };
                }
            }
        } catch (Exception e) {
            // В случае ошибки возвращаем русскую локаль по умолчанию
        }
        return Locale.of("ru"); // Русский по умолчанию
    }

    // Константы для ключей сообщений
    public static final class Keys {
        // Validation errors
        public static final String VALIDATION_FAILED = "validation.failed";
        public static final String VALIDATION_SIZE_10 = "validation.size.10";
        public static final String VALIDATION_SIZE_50 = "validation.size.50";
        public static final String VALIDATION_SIZE_255 = "validation.size.255";
        
        // Entity errors
        public static final String ENTITY_NOT_FOUND = "entity.not.found";
        public static final String ENTITY_ALREADY_EXISTS = "entity.already.exists";
        
        // System errors
        public static final String SYSTEM_ERROR = "system.error";
        public static final String ACCESS_DENIED = "access.denied";
    }
}
```

### 2. ValidationErrorDto.java
```java
package kg.bank.{project}.dto.validation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Ошибка валидации")
public class ValidationErrorDto {
    @Schema(description = "Поле с ошибкой", example = "name")
    private String field;
    
    @Schema(description = "Сообщение об ошибке", example = "Поле обязательно для заполнения")
    private String message;
}
```

### 3. GlobalHandlerException.java
```java
package kg.bank.{project}.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import kg.bank.{project}.controller.BaseController;
import kg.bank.{project}.dto.BaseResponse;
import kg.bank.{project}.dto.validation.ValidationErrorDto;
import kg.bank.{project}.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalHandlerException {
    
    private final MessageUtils messageUtils;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpServletRequest httpServletRequest) {

        List<ValidationErrorDto> validationErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::buildValidationError)
                .collect(Collectors.toList());

        return new ResponseEntity<>(BaseResponse.builder()
                .success(BaseController.Constants.ERROR)
                .msg(messageUtils.getMessage(MessageUtils.Keys.VALIDATION_FAILED))
                .res(validationErrors)
                .build(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse> handleRuntimeException(
            RuntimeException e, HttpServletRequest httpServletRequest) {
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.builder()
                        .success(BaseController.Constants.ERROR)
                        .msg(messageUtils.getMessage(MessageUtils.Keys.SYSTEM_ERROR))
                        .res(List.of(ValidationErrorDto.builder()
                                .message(e.getMessage())
                                .build()))
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleAllExceptions(
            Exception e, HttpServletRequest httpServletRequest) {
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.builder()
                        .success(BaseController.Constants.ERROR)
                        .msg(messageUtils.getMessage(MessageUtils.Keys.SYSTEM_ERROR))
                        .res(List.of(ValidationErrorDto.builder()
                                .message(e.getMessage())
                                .build()))
                        .build());
    }

    /**
     * Строит структурированную ошибку валидации с локализованным сообщением
     */
    private ValidationErrorDto buildValidationError(FieldError fieldError) {
        String messageKey = fieldError.getDefaultMessage();
        String localizedMessage = messageUtils.getMessage(messageKey);

        return ValidationErrorDto.builder()
                .field(fieldError.getField())
                .message(localizedMessage)
                .build();
    }
}
```

### 4. Файлы локализации

**LocalizationMessages.properties (русский по умолчанию):**
```properties
# Универсальные размеры валидации
validation.size.10=Не должно превышать 10 символов
validation.size.50=Не должно превышать 50 символов  
validation.size.255=Не должно превышать 255 символов

# Общие ошибки
validation.failed=Ошибка валидации
entity.not.found=Запись не найдена
entity.already.exists=Запись уже существует
system.error=Системная ошибка
access.denied=Доступ запрещен

# Поля сущностей
entity.name.required=Название обязательно для заполнения
entity.id.required=Идентификатор обязателен для заполнения
```

**LocalizationMessages_en.properties (английский):**
```properties
validation.size.10=Must not exceed 10 characters
validation.size.50=Must not exceed 50 characters
validation.size.255=Must not exceed 255 characters

validation.failed=Validation failed
entity.not.found=Entity not found
entity.already.exists=Entity already exists
system.error=System error
access.denied=Access denied

entity.name.required=Name is required
entity.id.required=ID is required
```

**LocalizationMessages_ky.properties (кыргызский):**
```properties
validation.size.10=10 символдон ашпоо керек
validation.size.50=50 символдон ашпоо керек
validation.size.255=255 символдон ашпоо керек

validation.failed=Текшерүү катасы
entity.not.found=Жазуу табылган жок
entity.already.exists=Жазуу мурунтан бар
system.error=Системалык ката
access.denied=Кирүүгө тыюу салынган

entity.name.required=Аты милдеттүү толтурулат
entity.id.required=Идентификатор милдеттүү
```

## 📝 Конфигурация Swagger/OpenAPI

### 1. OpenApiConfig.java
```java
package kg.bank.{project}.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("{Project} API")
                        .version("1.0")
                        .description("REST API для системы {project}"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", 
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")));
    }
}
```

### 2. Swagger настройки в application.properties
```properties
# Swagger/OpenAPI настройки
springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.filter=true
springdoc.swagger-ui.display-request-duration=true
springdoc.swagger-ui.deep-linking=true
springdoc.api-docs.version=openapi_3_0

# Локализация
spring.messages.basename=LocalizationMessages
spring.messages.encoding=UTF-8
spring.messages.fallback-to-system-locale=false
spring.web.locale=ru
spring.web.locale-resolver=fixed
```

## 🚀 Пошаговые инструкции по применению

### Шаг 1: Замена базовых классов
```bash
# 1. Создайте базовые DTO классы в пакете dto/
mkdir -p src/main/java/kg/bank/{project}/dto
mkdir -p src/main/java/kg/bank/{project}/dto/validation

# 2. Скопируйте готовые шаблоны:
# - BasePageRequest.java
# - BasePageResponse.java  
# - BaseResponse.java
# - ValidationErrorDto.java

# 3. Создайте BaseController.java в пакете controller/
mkdir -p src/main/java/kg/bank/{project}/controller
```

### Шаг 2: Обновление существующих контроллеров
```java
// ✅ НОВЫЙ стандарт
@RestController
@RequestMapping("/api/v1/users")
public class UserController extends BaseController {
    
    @PostMapping("/filter")
    public ResponseEntity<BaseResponse> filter(@RequestBody UserFilter filter) {
        PageUserResponse response = userService.filter(filter);
        return createSuccessResponse(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable Long id) {
        UserResponse response = userService.getById(id);
        return createSuccessResponse(response);
    }
}

// ❌ СТАРЫЙ подход (заменить)
@RestController
public class UserController {
    
    @PostMapping("/filter") 
    public List<User> filter(@RequestBody UserFilter filter) {
        return userService.filter(filter);
    }
    
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }
}
```

### Шаг 3: Создание DTO структуры
```bash
# Для каждой сущности создайте:
mkdir -p src/main/java/kg/bank/{project}/dto/{entity}/request
mkdir -p src/main/java/kg/bank/{project}/dto/{entity}/response  
mkdir -p src/main/java/kg/bank/{project}/dto/{entity}/filter

# Создайте файлы:
# - {Entity}Request.java
# - {Entity}Response.java  
# - Page{Entity}Response.java (наследует BasePageResponse)
# - {Entity}Filter.java (наследует BasePageRequest)
```

### Шаг 4: Создание JPA Specifications
```bash
mkdir -p src/main/java/kg/bank/{project}/db/repository/specifications

# Создайте {Entity}Specifications.java для каждой сущности
# Используйте шаблон из раздела "Пагинация и фильтрация"
```

### Шаг 5: Обновление мапперов
```java
// ✅ НОВЫЙ стандарт (@Component)
@Component
public class UserMapper {
    public UserResponse toResponse(User user) { ... }
    public User toEntity(UserRequest request) { ... }
    public void updateEntityFromRequest(UserRequest request, User user) { ... }
    public List<UserResponse> toResponseList(List<User> users) { ... }
    public PageUserResponse toPageResponse(Page<User> page) { ... }
}

// ❌ СТАРЫЙ подход (заменить)
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(UserRequest request);
}
```

### Шаг 6: Настройка системы локализации
```bash
# 1. Создайте MessageUtils.java в пакете util/
mkdir -p src/main/java/kg/bank/{project}/util

# 2. Создайте файлы локализации в resources/
touch src/main/resources/LocalizationMessages.properties
touch src/main/resources/LocalizationMessages_ky.properties

# 3. Создайте GlobalHandlerException.java в пакете exception/handler/
mkdir -p src/main/java/kg/bank/{project}/exception/handler
```

### Шаг 7: Обновление Gradle dependencies
```gradle
dependencies {
    // Убедитесь что есть все необходимые зависимости:
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui'
    implementation 'org.mapstruct:mapstruct'
    annotationProcessor 'org.mapstruct:mapstruct-processor'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

### Шаг 8: Настройка конфигурации
```properties
# Добавьте в application.properties:
spring.messages.basename=LocalizationMessages
spring.messages.encoding=UTF-8
spring.messages.fallback-to-system-locale=false
spring.web.locale=ru

springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.filter=true
springdoc.api-docs.version=openapi_3_0
```

## ✅ Чеклист проверки стандартизации

### Базовые классы
- [ ] BasePageRequest.java создан
- [ ] BasePageResponse.java создан  
- [ ] BaseResponse.java создан
- [ ] BaseController.java создан
- [ ] ValidationErrorDto.java создан

### Контроллеры
- [ ] Все контроллеры наследуют BaseController
- [ ] Используют createSuccessResponse/createErrorResponse
- [ ] Возвращают ResponseEntity<BaseResponse>
- [ ] Endpoint'ы фильтрации принимают Filter и возвращают Page{Entity}Response

### DTO структура
- [ ] Каждая сущность имеет request/response/filter пакеты
- [ ] Page{Entity}Response наследует BasePageResponse
- [ ] {Entity}Filter наследует BasePageRequest

### Мапперы
- [ ] Используют @Component вместо @Mapper
- [ ] Содержат все 5 обязательных методов
- [ ] Правильно устанавливают аудит поля

### Система локализации
- [ ] MessageUtils.java создан
- [ ] GlobalHandlerException.java создан
- [ ] Файлы LocalizationMessages*.properties созданы
- [ ] Конфигурация локализации добавлена в application.properties

### JPA Specifications  
- [ ] Specifications созданы для всех entities
- [ ] Включают фильтрацию по deleted = false
- [ ] Поддерживают все поля из соответствующего Filter

### Swagger/OpenAPI
- [ ] OpenApiConfig.java создан
- [ ] Swagger настройки добавлены в application.properties
- [ ] JWT авторизация настроена в Swagger

---

## 📋 Итоговые рекомендации

1. **Начните с базовых классов** - создайте BasePageRequest, BasePageResponse, BaseResponse
2. **Обновите по одному контроллеру** - переводите постепенно на новые стандарты
3. **Создайте мапперы с @Component** - замените автогенерацию MapStruct на ручные мапперы
4. **Настройте локализацию** - добавьте MessageUtils и файлы локализации
5. **Добавьте Specifications** - для всех entities с поддержкой фильтрации
6. **Протестируйте API** - убедитесь что все endpoint'ы возвращают единообразные ответы

**Результат**: Стандартизированный backend с единообразной архитектурой, готовыми шаблонами кода и полной поддержкой локализации.
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.batch_versioned_data=true

# Async/Scheduling - Многопоточность
spring.task.execution.pool.core-size=8
spring.task.execution.pool.max-size=32
spring.task.execution.pool.queue-capacity=200
spring.task.scheduling.pool.size=8
```

### Кэширование
```properties
# Caffeine Cache - Обязательные кэши
spring.cache.cache-names=positions,positionFilters,salykCompanyInfo,fiscaleProAuth
spring.cache.caffeine.spec=positions=maximumSize=500,expireAfterWrite=1h;positionFilters=maximumSize=100,expireAfterWrite=30m
```

### Интернационализация
```properties
# Мультиязычность (обязательно RU/KY)
spring.messages.basename=messages
spring.messages.encoding=UTF-8
spring.messages.fallback-to-system-locale=false
spring.web.locale=ru
spring.web.locale-resolver=fixed
```

## Система безопасности

### JWT Authentication
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Обработка токенов из заголовков:
    // - Authorization: Bearer <token>
}
```

### Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // CORS настройки для банковских приложений
    // JWT фильтры
    // Исключенные endpoints (/health, /swagger-ui/*)
}
```

## Интеграции с внешними системами

### Стандартная структура API клиентов
```java
@Service
public class ExternalApiClientService {
    private final WebClient webClient;
    
    @Cacheable("externalApiCache")
    public CompletableFuture<ApiResponse> getDataAsync(String param) {
        // Async вызов с кэшированием
    }
    
    @Retryable(value = {Exception.class}, maxAttempts = 3)
    public ApiResponse getDataWithRetry(String param) {
        // Retry логика для критичных операций
    }
}
```

### Обязательные интеграции
1. **Файловый сервер**: Загрузка/скачивание файлов
2. **Налоговая служба**: Получение данных компаний
3. **Банковская система**: SSO аутентификация

## Тестирование

### Структура тестов
```
src/test/java/
├── controller/          # MockMvc тесты REST API
├── service/            # Unit тесты бизнес-логики  
├── security/           # Тесты аутентификации
├── integration/        # Integration тесты с TestContainers
└── util/              # Тесты утилитарных классов
```

### Стандарты покрытия кода
- **Controllers**: 90%+ (критичные REST endpoints)
- **Services**: 80%+ (бизнес-логика)
- **Security**: 70%+ (аутентификация/авторизация)
- **Utilities**: 90%+ (вспомогательные функции)

### TestContainers конфигурация
```java
@Testcontainers
@SpringBootTest
class IntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");
}
```

## Мониторинг и логирование

### Actuator endpoints
```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when_authorized
```

### Логирование запросов
```java
@Entity
@Table(name = "sys_logs_request")
public class SysLogRequest {
    // Обязательные поля для аудита:
    // - endpoint, method, userId, companyId
    // - requestBody, responseBody, executionTime
    // - ipAddress, userAgent, timestamp
}
```

### Кастомные метрики
```java
@Component
public class CustomMetrics {
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
        Counter.builder("employees.created")
            .tag("company", event.getCompanyId().toString())
            .register(meterRegistry)
            .increment();
    }
}
```

## Бизнес-правила и ограничения

### Стандартные ограничения
- **Максимум сотрудников**: 50 на компанию
- **Размер файлов**: 5MB максимум
- **JWT токены**: 60 минут время жизни
- **Сессии**: Stateless (только JWT)

### Обязательные enum'ы
```java
public enum EmploymentStatusEnum {
    ACTIVE("Активный"),
    ON_LEAVE("В отпуске"), 
    TERMINATED("Уволен");
}

public enum DocumentTypeEnum {
    EMPLOYMENT_CONTRACT("Трудовой договор"),
    SALARY_CERTIFICATE("Справка о зарплате"),
    WORK_CERTIFICATE("Справка с места работы");
}
```

## Локализация

### Поддерживаемые языки
- **ru** (русский) - основной
- **ky** (кыргызский) - государственный язык

### Структура файлов локализации
```
messages.properties                    # Общие сообщения (русский)
messages_ky.properties                 # Общие сообщения (кыргызский)
```

### Automatic Accept-Language обработка
```java
@Component
public class MessageUtils {
    public String getMessage(String key, Object... args) {
        // Автоматическое определение языка из заголовка Accept-Language
        // Fallback на русский если язык не поддерживается
    }
}
```

## База данных

### Стандарты именования
- **Таблицы**: snake_case (companies, employees, hb_position)
- **Поля**: snake_case (first_name, created_at, company_id)
- **Индексы**: idx_{table}_{field} (idx_employees_company_id)
- **Внешние ключи**: fk_{table}_{referenced_table}

### Обязательные поля в основных таблицах
```sql
-- Аудит полей (обязательно)
created_at timestamp not null default now(),
created_by bigint,
updated_at timestamp,
updated_by bigint,

-- Soft delete (обязательно)
deleted boolean not null default false,
deleted_at timestamp,
deleted_by bigint,

-- Мультитенантность (если применимо)
company_id bigint not null references companies(id)
```

### Flyway миграции
- **Именование**: V{версия}__{описание}.sql (V1__initial_schema.sql)
- **Инкрементальные изменения**: Каждая миграция должна быть откатываемой
- **Данные**: Отдельные миграции для reference data (V1.1__insert_positions.sql)

## DevOps и развертывание

### Docker конфигурация
```dockerfile
FROM openjdk:21-jdk-slim
VOLUME /tmp
COPY target/app.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080
```

### Docker Compose для разработки

```yaml
version: '3.8'
services:
  app:
    build: ..
    ports:
      - "8080:8080"
    depends_on:
      - postgres
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: hr_widget_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres123
```

### Переменные окружения
```bash
# Обязательные переменные для production
JWT_SECRET=your-256-bit-secret
DB_HOST=localhost
DB_NAME=hr_widget_db
DB_USER=postgres
DB_PASSWORD=secure_password
FISCALEPRO_USERNAME=api_user
FISCALEPRO_PASSWORD=api_password
```

## Coding Standards

### Java конвенции
- **Пакеты**: lowercase (kg.bank.hrwidget.service)
- **Классы**: PascalCase (EmployeeService)
- **Методы**: camelCase (findActiveEmployees)
- **Константы**: UPPER_SNAKE_CASE (MAX_EMPLOYEES_PER_COMPANY)

### Аннотации документации
```java
/**
 * Сервис для управления сотрудниками компании.
 * Поддерживает CRUD операции с автоматической multi-tenant фильтрацией.
 * 
 * @author HR Widget Team
 * @version 1.0
 * @since 2024-07
 */
@Service
@Transactional
@Validated
public class EmployeeServiceImpl implements EmployeeService {
    // Реализация
}
```

### Обработка исключений
```java
@ControllerAdvice
public class GlobalHandlerException {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<BaseResponse<Object>> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        
        String message = messageUtils.getMessage("error.entity.not.found", ex.getEntityName());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(BaseResponse.error(message));
    }
}
```

## Документация API

### OpenAPI конфигурация
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("HR Widget API")
                .version("1.0")
                .description("REST API для системы управления персоналом"))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

### Swagger настройки
```properties
springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.filter=true
springdoc.swagger-ui.display-request-duration=true
springdoc.swagger-ui.deep-linking=true
springdoc.api-docs.version=openapi_3_0
```

---

## Чеклист для нового проекта

### ✅ Инициализация проекта
- [ ] Создать проект с Java 21 + Spring Boot 3.5.0
- [ ] Настроить Gradle с указанными зависимостями
- [ ] Создать стандартную структуру пакетов
- [ ] Настроить PostgreSQL 16 + Flyway
- [ ] Добавить базовые конфигурационные классы

### ✅ Безопасность и мультитенантность
- [ ] Реализовать JWT аутентификацию
- [ ] Создать TenantContextHolder
- [ ] Настроить CORS для банковских приложений
- [ ] Добавить SecurityConfig с правильными исключениями

### ✅ База данных
- [ ] Создать базовые сущности с soft delete
- [ ] Добавить аудит поля (created_at, updated_at, etc.)
- [ ] Настроить мультитенантность через company_id
- [ ] Создать Flyway миграции с правильным именованием

### ✅ Архитектурные паттерны
- [ ] Реализовать BaseController
- [ ] Создать универсальную систему валидации
- [ ] Настроить MapStruct мапперы
- [ ] Добавить GlobalHandlerException

### ✅ Локализация
- [ ] Создать файлы локализации (ru/en/ky)
- [ ] Настроить MessageUtils с Accept-Language
- [ ] Добавить локализованные сообщения валидации
- [ ] Протестировать переключение языков

### ✅ Тестирование
- [ ] Настроить TestContainers с PostgreSQL
- [ ] Создать базовые тесты контроллеров
- [ ] Добавить JWT тестовые утилиты
- [ ] Настроить JaCoCo для покрытия кода

### ✅ Мониторинг
- [ ] Настроить Actuator endpoints
- [ ] Добавить систему логирования запросов
- [ ] Создать кастомные метрики
- [ ] Настроить health checks

### ✅ Документация
- [ ] Настроить Swagger/OpenAPI
- [ ] Создать README с инструкциями
- [ ] Добавить примеры API запросов
- [ ] Документировать конфигурационные параметры

---

*Данный документ является обязательным стандартом для всех backend проектов банковских HR систем. При создании нового проекта используйте этот документ как чеклист и референс для архитектурных решений.*
