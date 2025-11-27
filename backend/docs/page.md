# Pagination System - Руководство по реализации

## Обзор

Система пагинации с фильтрацией на основе Spring Data JPA Specifications.

**Ключевые особенности:**
- 1-based нумерация страниц в API (конвертируется в 0-based для JPA)
- Наследование фильтров от BasePageRequest
- Generic response wrappers
- JPA Specifications для сложных запросов
- Поддержка мультитенантности

---

## Архитектура

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Controller    │────▶│    Service      │────▶│   Repository    │
│  POST /filter   │     │   filter()      │     │   findAll()     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                       │                       │
        ▼                       ▼                       ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  EntityFilter   │     │  Specification  │     │   Page<Entity>  │
│ extends BaseReq │     │  withFilter()   │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                                               │
        ▼                                               ▼
┌─────────────────┐                             ┌─────────────────┐
│ BasePageRequest │                             │ BasePageResponse│
│ page, size      │                             │ toPageResponse()│
└─────────────────┘                             └─────────────────┘
```

---

## 1. Базовые классы

### BasePageRequest.java

```java
package kg.bishkek.support.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Базовый класс для всех фильтров с пагинацией
 * Все фильтры должны наследовать этот класс
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BasePageRequest {

    @Schema(description = "Номер страницы (начиная с 1)", example = "1", defaultValue = "1")
    private Integer page = 1;

    @Schema(description = "Размер страницы", example = "15", defaultValue = "15")
    private Integer size = 15;
}
```

### BasePageResponse.java

```java
package kg.bishkek.support.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Базовый класс для ответов с пагинацией
 * Содержит метаданные о странице и общем количестве элементов
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BasePageResponse<T> {

    @Schema(description = "Текущая страница (1-based)", example = "1")
    private int page;

    @Schema(description = "Размер страницы", example = "15")
    private int size;

    @Schema(description = "Общее количество элементов", example = "100")
    private long totalElements;

    @Schema(description = "Общее количество страниц", example = "7")
    private int totalPages;

    @Schema(description = "Данные страницы")
    private List<T> content;
}
```

### BaseResponse.java (обертка для API)

```java
package kg.bishkek.support.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Унифицированный формат ответа API
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    @Schema(description = "Успешность операции", example = "true")
    private boolean success;

    @Schema(description = "Сообщение (для ошибок)")
    private Object message;

    @Schema(description = "Результат операции")
    private T result;

    @Schema(description = "Время ответа", example = "2025-01-31 15:30:00")
    @Builder.Default
    private String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    @Schema(description = "Версия API", example = "1.0.0")
    @Builder.Default
    private String ver = "1.0.0";
}
```

---

## 2. BaseController

```java
package kg.bishkek.support.controller;

import kg.bishkek.support.dto.common.BaseResponse;
import org.springframework.http.ResponseEntity;

/**
 * Базовый контроллер с утилитами для пагинации и ответов
 */
public abstract class BaseController {

    /**
     * Конвертирует 1-based номер страницы в 0-based для JPA
     *
     * @param page номер страницы (1-based)
     * @return номер страницы (0-based)
     */
    public static Integer getPage(Integer page) {
        if (page != null && page > 0) {
            return page - 1;
        }
        return 0;
    }

    /**
     * Создает успешный ответ
     */
    protected <T> ResponseEntity<BaseResponse<T>> success(T data) {
        return ResponseEntity.ok(BaseResponse.<T>builder()
                .success(true)
                .result(data)
                .build());
    }

    /**
     * Создает успешный ответ с сообщением
     */
    protected <T> ResponseEntity<BaseResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(BaseResponse.<T>builder()
                .success(true)
                .result(data)
                .message(message)
                .build());
    }

    /**
     * Создает ответ с ошибкой
     */
    protected <T> ResponseEntity<BaseResponse<T>> error(String message) {
        return ResponseEntity.badRequest().body(BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .build());
    }
}
```

---

## 3. Пример Filter класса

### UserFilter.java

```java
package kg.bishkek.support.dto.user.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import kg.bishkek.support.dto.common.BasePageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO для фильтрации пользователей
 * Наследует page и size от BasePageRequest
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Фильтр для поиска пользователей")
public class UserFilter extends BasePageRequest {

    @Schema(description = "Имя пользователя (частичное совпадение)", example = "operator")
    private String username;

    @Schema(description = "Email (частичное совпадение)", example = "azs-01")
    private String email;

    @Schema(description = "Имя или фамилия (частичное совпадение)", example = "Иван")
    private String name;

    @Schema(description = "Роль пользователя", example = "ADMIN")
    private String role;

    @Schema(description = "ID организации", example = "1")
    private Long organizationId;

    @Schema(description = "Только активные пользователи", example = "true")
    private Boolean activeOnly;

    @Schema(description = "Телефон (частичное совпадение)", example = "555")
    private String phone;
}
```

### TicketFilter.java (более сложный пример)

```java
package kg.bishkek.support.dto.ticket.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import kg.bishkek.support.db.enums.Priority;
import kg.bishkek.support.db.enums.TicketStatus;
import kg.bishkek.support.dto.common.BasePageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Фильтр для поиска заявок")
public class TicketFilter extends BasePageRequest {

    @Schema(description = "Номер заявки", example = "TKT-2025-0001")
    private String ticketNumber;

    @Schema(description = "Статус заявки", example = "NEW")
    private TicketStatus status;

    @Schema(description = "Приоритет", example = "HIGH")
    private Priority priority;

    @Schema(description = "ID исполнителя", example = "5")
    private Long assigneeId;

    @Schema(description = "Дата создания от", example = "2025-01-01T00:00:00")
    private LocalDateTime createdFrom;

    @Schema(description = "Дата создания до", example = "2025-12-31T23:59:59")
    private LocalDateTime createdTo;

    @Schema(description = "Поиск по тексту (заголовок, описание)", example = "принтер")
    private String searchText;

    @Schema(description = "Только активные заявки", example = "true")
    private Boolean activeOnly;
}
```

---

## 4. Типизированные PageResponse классы

### PageUserResponse.java

```java
package kg.bishkek.support.dto.user.response;

import kg.bishkek.support.dto.common.BasePageResponse;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Типизированный ответ с пагинацией для пользователей
 * Нужен для корректной генерации Swagger документации
 */
@SuperBuilder
@NoArgsConstructor
public class PageUserResponse extends BasePageResponse<UserResponse> {

    public PageUserResponse(BasePageResponse<UserResponse> baseResponse) {
        this.setContent(baseResponse.getContent());
        this.setTotalElements(baseResponse.getTotalElements());
        this.setTotalPages(baseResponse.getTotalPages());
        this.setSize(baseResponse.getSize());
        this.setPage(baseResponse.getPage());
    }
}
```

### PageTicketResponse.java

```java
package kg.bishkek.support.dto.ticket.response;

import kg.bishkek.support.dto.common.BasePageResponse;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
public class PageTicketResponse extends BasePageResponse<TicketResponse> {

    public PageTicketResponse(BasePageResponse<TicketResponse> baseResponse) {
        this.setContent(baseResponse.getContent());
        this.setTotalElements(baseResponse.getTotalElements());
        this.setTotalPages(baseResponse.getTotalPages());
        this.setSize(baseResponse.getSize());
        this.setPage(baseResponse.getPage());
    }
}
```

---

## 5. Mapper с toPageResponse

### UserMapper.java

```java
package kg.bishkek.support.mapper;

import kg.bishkek.support.db.entity.User;
import kg.bishkek.support.dto.common.BasePageResponse;
import kg.bishkek.support.dto.user.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    /**
     * Конвертирует Entity в Response DTO
     */
    public UserResponse toResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Конвертирует список Entity в список Response DTO
     */
    public List<UserResponse> toResponseList(List<User> users) {
        if (users == null) return null;
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Конвертирует Page<Entity> в BasePageResponse<Response>
     *
     * ВАЖНО: Конвертирует 0-based page обратно в 1-based
     */
    public BasePageResponse<UserResponse> toPageResponse(Page<User> page) {
        if (page == null) return null;

        return BasePageResponse.<UserResponse>builder()
                .content(toResponseList(page.getContent()))
                .page(page.getNumber() + 1)  // 0-based -> 1-based
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
```

---

## 6. JPA Specifications

### BaseSpecifications.java

```java
package kg.bishkek.support.db.repository.specifications;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

/**
 * Базовые спецификации для всех сущностей
 */
public class BaseSpecifications {

    /**
     * Фильтр для исключения удаленных записей (soft delete)
     */
    public static <T> Specification<T> notDeleted() {
        return (root, query, cb) -> {
            if (hasAttribute(root, "deleted")) {
                return cb.isFalse(root.get("deleted"));
            }
            return cb.conjunction(); // TRUE - не применяем фильтр
        };
    }

    /**
     * Проверка наличия текста
     */
    public static boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Проверка наличия атрибута у сущности
     */
    private static <T> boolean hasAttribute(Root<T> root, String attributeName) {
        try {
            root.get(attributeName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
```

### UserSpecifications.java

```java
package kg.bishkek.support.db.repository.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import kg.bishkek.support.db.entity.User;
import kg.bishkek.support.dto.user.filter.UserFilter;
import org.springframework.data.jpa.domain.Specification;

import static kg.bishkek.support.db.repository.specifications.BaseSpecifications.hasText;
import static kg.bishkek.support.db.repository.specifications.BaseSpecifications.notDeleted;

/**
 * JPA Specifications для фильтрации пользователей
 */
public class UserSpecifications {

    /**
     * Фильтрация по имени пользователя (LIKE, case-insensitive)
     */
    public static Specification<User> byUsername(String username) {
        return (root, query, cb) -> {
            if (!hasText(username)) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("username")),
                    "%" + username.toLowerCase() + "%"
            );
        };
    }

    /**
     * Фильтрация по email (LIKE, case-insensitive)
     */
    public static Specification<User> byEmail(String email) {
        return (root, query, cb) -> {
            if (!hasText(email)) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("email")),
                    "%" + email.toLowerCase() + "%"
            );
        };
    }

    /**
     * Фильтрация по полному имени (LIKE, case-insensitive)
     */
    public static Specification<User> byFullName(String fullName) {
        return (root, query, cb) -> {
            if (!hasText(fullName)) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("fullName")),
                    "%" + fullName.toLowerCase() + "%"
            );
        };
    }

    /**
     * Фильтрация по роли (через JOIN)
     */
    public static Specification<User> byRole(String roleCode) {
        return (root, query, cb) -> {
            if (!hasText(roleCode)) {
                return cb.conjunction();
            }
            Join<Object, Object> userRoleJoin = root.join("userRoles", JoinType.INNER);
            Join<Object, Object> roleJoin = userRoleJoin.join("role", JoinType.INNER);

            return cb.and(
                    cb.equal(userRoleJoin.get("active"), true),
                    cb.equal(roleJoin.get("code"), roleCode)
            );
        };
    }

    /**
     * Фильтрация только активных
     */
    public static Specification<User> byActiveOnly(Boolean activeOnly) {
        return (root, query, cb) -> {
            if (activeOnly == null || !activeOnly) {
                return cb.conjunction();
            }
            return cb.equal(root.get("active"), true);
        };
    }

    /**
     * Фильтрация по телефону (LIKE)
     */
    public static Specification<User> byPhone(String phone) {
        return (root, query, cb) -> {
            if (!hasText(phone)) {
                return cb.conjunction();
            }
            return cb.like(root.get("phone"), "%" + phone + "%");
        };
    }

    /**
     * Комплексная фильтрация - собирает все условия
     */
    public static Specification<User> withFilter(UserFilter filter) {
        return notDeleted()
                .and(byUsername(filter != null ? filter.getUsername() : null))
                .and(byEmail(filter != null ? filter.getEmail() : null))
                .and(byFullName(filter != null ? filter.getName() : null))
                .and(byRole(filter != null ? filter.getRole() : null))
                .and(byActiveOnly(filter != null ? filter.getActiveOnly() : null))
                .and(byPhone(filter != null ? filter.getPhone() : null));
    }
}
```

### TicketSpecifications.java (расширенный пример)

```java
package kg.bishkek.support.db.repository.specifications;

import kg.bishkek.support.db.entity.Ticket;
import kg.bishkek.support.db.enums.Priority;
import kg.bishkek.support.db.enums.TicketStatus;
import kg.bishkek.support.dto.ticket.filter.TicketFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

import static kg.bishkek.support.db.repository.specifications.BaseSpecifications.hasText;
import static kg.bishkek.support.db.repository.specifications.BaseSpecifications.notDeleted;

public class TicketSpecifications {

    public static Specification<Ticket> byTicketNumber(String ticketNumber) {
        return (root, query, cb) -> {
            if (!hasText(ticketNumber)) return cb.conjunction();
            return cb.like(
                    cb.lower(root.get("ticketNumber")),
                    "%" + ticketNumber.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Ticket> byStatus(TicketStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Ticket> byPriority(Priority priority) {
        return (root, query, cb) -> {
            if (priority == null) return cb.conjunction();
            return cb.equal(root.get("priority"), priority);
        };
    }

    public static Specification<Ticket> byAssigneeId(Long assigneeId) {
        return (root, query, cb) -> {
            if (assigneeId == null) return cb.conjunction();
            return cb.equal(root.get("assignee").get("id"), assigneeId);
        };
    }

    public static Specification<Ticket> byCreatedFrom(LocalDateTime from) {
        return (root, query, cb) -> {
            if (from == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
        };
    }

    public static Specification<Ticket> byCreatedTo(LocalDateTime to) {
        return (root, query, cb) -> {
            if (to == null) return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    /**
     * Поиск по тексту в нескольких полях
     */
    public static Specification<Ticket> bySearchText(String searchText) {
        return (root, query, cb) -> {
            if (!hasText(searchText)) return cb.conjunction();
            String pattern = "%" + searchText.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("ticketNumber")), pattern)
            );
        };
    }

    /**
     * Только активные (не закрытые/отмененные)
     */
    public static Specification<Ticket> activeOnly(Boolean active) {
        return (root, query, cb) -> {
            if (active == null || !active) return cb.conjunction();
            return root.get("status").in(
                    TicketStatus.NEW,
                    TicketStatus.ASSIGNED,
                    TicketStatus.IN_PROGRESS,
                    TicketStatus.IN_QUEUE
            );
        };
    }

    public static Specification<Ticket> withFilter(TicketFilter filter) {
        return notDeleted()
                .and(byTicketNumber(filter != null ? filter.getTicketNumber() : null))
                .and(byStatus(filter != null ? filter.getStatus() : null))
                .and(byPriority(filter != null ? filter.getPriority() : null))
                .and(byAssigneeId(filter != null ? filter.getAssigneeId() : null))
                .and(byCreatedFrom(filter != null ? filter.getCreatedFrom() : null))
                .and(byCreatedTo(filter != null ? filter.getCreatedTo() : null))
                .and(bySearchText(filter != null ? filter.getSearchText() : null))
                .and(activeOnly(filter != null ? filter.getActiveOnly() : null));
    }
}
```

---

## 7. Service Implementation

### UserServiceImpl.java

```java
package kg.bishkek.support.service.impl;

import kg.bishkek.support.controller.BaseController;
import kg.bishkek.support.db.entity.User;
import kg.bishkek.support.db.repository.UserRepository;
import kg.bishkek.support.db.repository.specifications.UserSpecifications;
import kg.bishkek.support.dto.user.filter.UserFilter;
import kg.bishkek.support.dto.user.response.PageUserResponse;
import kg.bishkek.support.mapper.UserMapper;
import kg.bishkek.support.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public PageUserResponse filter(UserFilter filter) {
        log.debug("Filtering users: username={}, email={}, role={}, activeOnly={}",
                filter.getUsername(), filter.getEmail(), filter.getRole(), filter.getActiveOnly());

        // 1. Строим Specification из фильтра
        Specification<User> spec = UserSpecifications.withFilter(filter);

        // 2. Создаем Pageable (конвертируем 1-based в 0-based)
        Pageable pageable = PageRequest.of(
                BaseController.getPage(filter.getPage()),
                filter.getSize() != null ? filter.getSize() : 15,
                Sort.by(Sort.Direction.ASC, "fullName")  // Сортировка по умолчанию
        );

        // 3. Выполняем запрос
        Page<User> usersPage = userRepository.findAll(spec, pageable);

        log.debug("Found {} users", usersPage.getTotalElements());

        // 4. Конвертируем в Response
        return new PageUserResponse(userMapper.toPageResponse(usersPage));
    }
}
```

### TicketServiceImpl.java (с ролевым доступом)

```java
@Override
@Transactional(readOnly = true)
public PageTicketResponse filter(TicketFilter filter) {
    // 1. Базовая спецификация из фильтра
    Specification<Ticket> spec = TicketSpecifications.withFilter(filter);

    // 2. Добавляем ролевой доступ
    User currentUser = userService.getFromContext();

    if (currentUser.hasRole("TECHNICIAN")) {
        // Техники видят только свои заявки
        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("assignee").get("id"), currentUser.getId())
        );
    } else if (currentUser.hasRole("OPERATOR")) {
        // Операторы видят только созданные ими заявки
        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("reporter").get("id"), currentUser.getId())
        );
    }
    // Диспетчеры и админы видят все (без дополнительных фильтров)

    // 3. Пагинация с сортировкой
    Pageable pageable = PageRequest.of(
            BaseController.getPage(filter.getPage()),
            filter.getSize() != null ? filter.getSize() : 15,
            Sort.by(Sort.Direction.DESC, "createdAt")
    );

    // 4. Выполняем запрос
    Page<Ticket> ticketsPage = ticketRepository.findAll(spec, pageable);

    return ticketMapper.toPageResponse(ticketsPage);
}
```

---

## 8. Controller

### UserController.java

```java
package kg.bishkek.support.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kg.bishkek.support.dto.common.BaseResponse;
import kg.bishkek.support.dto.user.filter.UserFilter;
import kg.bishkek.support.dto.user.response.PageUserResponse;
import kg.bishkek.support.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API управления пользователями")
public class UserController extends BaseController {

    private final UserService userService;

    /**
     * Фильтрация пользователей с пагинацией
     *
     * POST используется вместо GET для поддержки сложных фильтров в body
     */
    @PostMapping("/filter")
    @Operation(summary = "Фильтрация пользователей",
               description = "Поиск пользователей с фильтрацией и пагинацией")
    public ResponseEntity<BaseResponse<PageUserResponse>> filterUsers(
            @RequestBody UserFilter filter) {

        PageUserResponse users = userService.filter(filter);
        return success(users);
    }
}
```

---

## 9. Repository

```java
package kg.bishkek.support.db.repository;

import kg.bishkek.support.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * ВАЖНО: Наследуем JpaSpecificationExecutor для поддержки Specifications
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>,
                                        JpaSpecificationExecutor<User> {
    // Стандартные методы JPA + поддержка findAll(Specification, Pageable)
}
```

---

## 10. Пример API запроса/ответа

### Request

```http
POST /api/users/filter
Content-Type: application/json

{
    "page": 1,
    "size": 10,
    "username": "ivan",
    "role": "ADMIN",
    "activeOnly": true
}
```

### Response

```json
{
    "success": true,
    "result": {
        "page": 1,
        "size": 10,
        "totalElements": 25,
        "totalPages": 3,
        "content": [
            {
                "id": 1,
                "username": "ivan.petrov",
                "email": "ivan@example.com",
                "fullName": "Иван Петров",
                "active": true
            },
            {
                "id": 5,
                "username": "ivan.sidorov",
                "email": "sidorov@example.com",
                "fullName": "Иван Сидоров",
                "active": true
            }
        ]
    },
    "time": "2025-01-31 15:30:00",
    "ver": "1.0.0"
}
```

---

## 11. Чек-лист для внедрения

- [ ] Создать BasePageRequest
- [ ] Создать BasePageResponse
- [ ] Создать BaseResponse
- [ ] Создать BaseController с методом getPage()
- [ ] Создать BaseSpecifications
- [ ] Для каждой сущности:
  - [ ] Создать EntityFilter extends BasePageRequest
  - [ ] Создать PageEntityResponse extends BasePageResponse<EntityResponse>
  - [ ] Создать EntitySpecifications с withFilter()
  - [ ] Добавить JpaSpecificationExecutor в Repository
  - [ ] Реализовать filter() в Service
  - [ ] Добавить POST /filter в Controller
  - [ ] Добавить toPageResponse() в Mapper

---

## 12. Полезные паттерны

### Динамическая сортировка

```java
// В Filter добавить поля:
private String sortBy = "createdAt";
private String sortDirection = "DESC";

// В Service:
Sort sort = Sort.by(
    "ASC".equalsIgnoreCase(filter.getSortDirection())
        ? Sort.Direction.ASC
        : Sort.Direction.DESC,
    filter.getSortBy()
);

Pageable pageable = PageRequest.of(
    BaseController.getPage(filter.getPage()),
    filter.getSize(),
    sort
);
```

### Множественные статусы

```java
// В Filter:
private List<TicketStatus> statuses;

// В Specification:
public static Specification<Ticket> byStatuses(List<TicketStatus> statuses) {
    return (root, query, cb) -> {
        if (statuses == null || statuses.isEmpty()) return cb.conjunction();
        return root.get("status").in(statuses);
    };
}
```

### Диапазон дат

```java
public static Specification<Ticket> byDateRange(LocalDateTime from, LocalDateTime to) {
    return (root, query, cb) -> {
        if (from == null && to == null) return cb.conjunction();
        if (from != null && to != null) {
            return cb.between(root.get("createdAt"), from, to);
        }
        if (from != null) {
            return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
        }
        return cb.lessThanOrEqualTo(root.get("createdAt"), to);
    };
}
```
