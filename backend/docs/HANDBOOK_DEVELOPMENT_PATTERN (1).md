# Паттерн разработки справочников (Handbook Development Pattern)

> **Инструкция для Claude Code**: Все новые справочники должны создаваться строго по этому паттерну для обеспечения единообразия архитектуры.

## Обзор архитектуры

Система использует единый паттерн для всех справочников, включающий:
- **Entity** (наследует от `BaseHandbook`)
- **Repository** (наследует от `BaseHandbookRepository<T>`)
- **Service Interface** (стандартные методы)
- **Service Implementation** (использует базовые методы)
- **Controller** (стандартные REST endpoints)
- **DTO** (Request, Response, Filter, PageResponse)

## Структура файлов

Для справочника `XxxYyy` создать следующие файлы:

```
src/main/java/kg/bishkekpetroleum/salesmanagement/
├── db/entity/hb/HBXxxYyy.java
├── db/repository/hb/HBXxxYyyRepository.java
├── service/hb/XxxYyyService.java
├── service/hb/impl/XxxYyyServiceImpl.java
├── controller/hb/XxxYyyController.java
├── dto/hb/xxxyyy/
│   ├── request/XxxYyyRequest.java
│   ├── response/XxxYyyResponse.java
│   ├── response/PageXxxYyyResponse.java
│   └── filter/XxxYyyFilter.java
└── mapper/hb/XxxYyyMapper.java
```

## 1. Entity (наследует BaseHandbook)

```java
package kg.bishkekpetroleum.salesmanagement.db.entity.hb;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for XxxYyy handbook.
 *
 * @author Bishkek Petroleum Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "hb_xxx_yyy")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HBXxxYyy extends BaseHandbook {

    // Дополнительные поля специфичные для справочника
    // Базовые поля (id, alias, nameRu, nameKy, deleted, createdAt, updatedAt) наследуются
    
    @Column(name = "specific_field")
    private String specificField;
}
```

## 2. Repository (наследует BaseHandbookRepository)

```java
package kg.bishkekpetroleum.salesmanagement.db.repository.hb;

import kg.bishkekpetroleum.salesmanagement.db.entity.hb.HBXxxYyy;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for HBXxxYyy entity operations.
 * Extends BaseHandbookRepository to inherit common handbook operations.
 *
 * @author Bishkek Petroleum Development Team
 * @since 1.0.0
 */
@Repository
public interface HBXxxYyyRepository extends BaseHandbookRepository<HBXxxYyy> {

    // Базовые методы наследуются от BaseHandbookRepository:
    // - findByAliasAndDeletedFalse(String alias)
    // - findAllByDeletedFalse()
    // - findAllByDeletedFalseOrderByNameRu()
    // - existsByAliasAndDeletedFalse(String alias)

    // Добавлять только специфичные методы, если необходимо
    Optional<HBXxxYyy> findBySpecificFieldAndDeletedFalse(String specificField);
}
```

## 3. Service Interface (паттерн A)

```java
package kg.bishkekpetroleum.salesmanagement.service.hb;

import jakarta.servlet.http.HttpServletRequest;
import kg.bishkekpetroleum.salesmanagement.db.entity.hb.HBXxxYyy;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.filter.XxxYyyFilter;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.request.XxxYyyRequest;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.response.XxxYyyResponse;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.response.PageXxxYyyResponse;

/**
 * Service interface for managing XxxYyy handbook.
 *
 * @author Bishkek Petroleum Development Team
 * @since 1.0.0
 */
public interface XxxYyyService {

    /**
     * Creates a new XxxYyy.
     */
    XxxYyyResponse create(XxxYyyRequest request, HttpServletRequest httpServletRequest);

    /**
     * Updates an existing XxxYyy.
     */
    XxxYyyResponse update(XxxYyyRequest request, HttpServletRequest httpServletRequest);

    /**
     * Soft deletes a XxxYyy by ID.
     */
    void delete(Long id, HttpServletRequest httpServletRequest);

    /**
     * Gets a XxxYyy by ID.
     */
    XxxYyyResponse get(Long id);

    /**
     * Finds XxxYyy entity by ID (for internal use).
     */
    HBXxxYyy findById(Long id);

    /**
     * Finds all XxxYyy with filter and pagination.
     */
    PageXxxYyyResponse findAllWithFilter(XxxYyyFilter filter);
}
```

## 4. Service Implementation (использует BaseHandbookRepository методы)

```java
package kg.bishkekpetroleum.salesmanagement.service.hb.impl;

import jakarta.servlet.http.HttpServletRequest;
import kg.bishkekpetroleum.salesmanagement.db.entity.hb.HBXxxYyy;
import kg.bishkekpetroleum.salesmanagement.db.repository.hb.HBXxxYyyRepository;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.filter.XxxYyyFilter;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.request.XxxYyyRequest;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.response.XxxYyyResponse;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.response.PageXxxYyyResponse;
import kg.bishkekpetroleum.salesmanagement.exception.EntityExistsException;
import kg.bishkekpetroleum.salesmanagement.exception.ResourceNotFoundException;
import kg.bishkekpetroleum.salesmanagement.mapper.hb.XxxYyyMapper;
import kg.bishkekpetroleum.salesmanagement.service.SysLogsRequestService;
import kg.bishkekpetroleum.salesmanagement.service.hb.XxxYyyService;
import kg.bishkekpetroleum.salesmanagement.util.HandbookAliasUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of XxxYyyService.
 * Provides business logic for XxxYyy handbook operations.
 *
 * @author Bishkek Petroleum Development Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class XxxYyyServiceImpl implements XxxYyyService {

    private final HBXxxYyyRepository repository;
    private final XxxYyyMapper mapper;
    private final SysLogsRequestService logService;
    private final HandbookAliasUtils aliasUtils;

    @Override
    @Transactional
    public XxxYyyResponse create(XxxYyyRequest request, HttpServletRequest httpServletRequest) {
        log.debug("Creating new XxxYyy with nameRu: {}", request.getNameRu());

        // Проверка на дубликаты - используем BaseHandbookRepository методы
        Optional<HBXxxYyy> existing = repository.findByNameRuIgnoreCaseAndNameKyIgnoreCase(
                request.getNameRu(), request.getNameKy());
        
        if (existing.isPresent() && !existing.get().isDeleted()) {
            throw new EntityExistsException("XxxYyy with this name already exists");
        }

        HBXxxYyy entity;
        if (existing.isPresent()) {
            // Восстановление удаленной записи
            entity = existing.get();
            mapper.updateFromRequest(request, entity);
            entity.setDeleted(false);
        } else {
            // Создание новой записи
            entity = mapper.toEntity(request);
        }

        // Автоматическая генерация алиаса
        String alias = aliasUtils.generateUniqueAlias(request.getNameRu(), repository, 
                existing.isPresent() ? entity.getId() : null);
        entity.setAlias(alias);
        
        entity.setDeleted(false);
        if (existing.isPresent()) {
            entity.setUpdatedAt(LocalDateTime.now());
        } else {
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
        }

        HBXxxYyy saved = repository.save(entity);
        log.info("Created XxxYyy with ID: {} and alias: {}", saved.getId(), saved.getAlias());
        
        logService.saveSuccessToDb(
                this.getClass().getSimpleName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                "XxxYyy created: " + saved.getAlias(),
                httpServletRequest);

        return mapper.toResponse(saved);
    }

    @Override
    @CacheEvict(value = {"xxxYyys", "xxxYyyFilters"}, allEntries = true)
    @Transactional
    public XxxYyyResponse update(XxxYyyRequest request, HttpServletRequest httpServletRequest) {
        log.debug("Updating XxxYyy with ID: {}", request.getId());

        HBXxxYyy existing = findById(request.getId());

        String currentNameRu = existing.getNameRu();
        String newNameRu = request.getNameRu();
        String currentNameKy = existing.getNameKy();
        String newNameKy = request.getNameKy();

        // Проверка на дубликаты при изменении имен
        if (!currentNameRu.equals(newNameRu) || !currentNameKy.equals(newNameKy)) {
            Optional<HBXxxYyy> duplicate = repository.findByNameRuIgnoreCaseAndNameKyIgnoreCase(
                    newNameRu, newNameKy);
            
            if (duplicate.isPresent() && !duplicate.get().getId().equals(request.getId()) 
                && !duplicate.get().isDeleted()) {
                throw new EntityExistsException("XxxYyy with this name already exists");
            }
        }

        mapper.updateFromRequest(request, existing);

        // Перегенерация алиаса при изменении русского имени
        if (!currentNameRu.equals(newNameRu)) {
            String newAlias = aliasUtils.generateUniqueAlias(newNameRu, repository, existing.getId());
            existing.setAlias(newAlias);
        }

        existing.setUpdatedAt(LocalDateTime.now());
        HBXxxYyy saved = repository.save(existing);
        log.info("Updated XxxYyy with ID: {}", saved.getId());

        logService.saveSuccessToDb(
                this.getClass().getSimpleName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                "XxxYyy updated: " + saved.getAlias(),
                httpServletRequest);

        return mapper.toResponse(saved);
    }

    @Override
    @CacheEvict(value = {"xxxYyys", "xxxYyyFilters"}, allEntries = true)
    @Transactional
    public void delete(Long id, HttpServletRequest httpServletRequest) {
        log.debug("Soft deleting XxxYyy with ID: {}", id);

        HBXxxYyy entity = findById(id);
        entity.setDeleted(true);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
        
        log.info("Soft deleted XxxYyy with ID: {}", id);
        
        logService.saveSuccessToDb(
                this.getClass().getSimpleName(),
                Thread.currentThread().getStackTrace()[1].getMethodName(),
                "XxxYyy deleted: " + entity.getAlias(),
                httpServletRequest);
    }

    @Override
    @Cacheable(value = "xxxYyys", key = "#id")
    public XxxYyyResponse get(Long id) {
        log.debug("Getting XxxYyy with ID: {}", id);
        return mapper.toResponse(findById(id));
    }

    @Override
    public HBXxxYyy findById(Long id) {
        return repository.findById(id)
                .filter(entity -> !entity.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("XxxYyy not found with id: " + id));
    }

    @Override
    @Cacheable(value = "xxxYyyFilters")
    public PageXxxYyyResponse findAllWithFilter(XxxYyyFilter filter) {
        // Использовать фильтр для построения запроса
        Page<HBXxxYyy> page = repository.findAll(
                PageRequest.of(filter.getPage(), filter.getSize(), 
                        Sort.by(Sort.Direction.ASC, "nameRu")));
        
        List<XxxYyyResponse> content = page.getContent().stream()
                .filter(entity -> !entity.isDeleted())
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        
        PageXxxYyyResponse response = new PageXxxYyyResponse();
        response.setContent(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        
        return response;
    }
}
```

## 5. Controller (стандартные REST endpoints)

```java
package kg.bishkekpetroleum.salesmanagement.controller.hb;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kg.bishkekpetroleum.salesmanagement.controller.BaseController;
import kg.bishkekpetroleum.salesmanagement.dto.BaseResponse;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.filter.XxxYyyFilter;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.request.XxxYyyRequest;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.response.XxxYyyResponse;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.response.PageXxxYyyResponse;
import kg.bishkekpetroleum.salesmanagement.service.hb.XxxYyyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing XxxYyy handbook.
 *
 * @author Bishkek Petroleum Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/handbooks/xxx-yyy")
@RequiredArgsConstructor
@Tag(name = "Справочник XxxYyy", description = "Управление справочником XxxYyy")
public class XxxYyyController extends BaseController {

    private final XxxYyyService service;

    @PostMapping
    @Operation(
            summary = "Создать XxxYyy",
            description = "Создание новой записи XxxYyy в справочнике",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = XxxYyyResponse.class))),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "DTO для создания XxxYyy",
                    content = @Content(schema = @Schema(implementation = XxxYyyRequest.class))
            )
    )
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> create(@Valid @RequestBody XxxYyyRequest request,
                                               HttpServletRequest httpServletRequest) {
        var response = service.create(request, httpServletRequest);
        return createSuccessResponse(response);
    }

    @PutMapping
    @Operation(
            summary = "Обновить XxxYyy",
            description = "Обновление существующей записи XxxYyy в справочнике",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = XxxYyyResponse.class))),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "DTO для обновления XxxYyy",
                    content = @Content(schema = @Schema(implementation = XxxYyyRequest.class))
            )
    )
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> update(@Valid @RequestBody XxxYyyRequest request,
                                               HttpServletRequest httpServletRequest) {
        var response = service.update(request, httpServletRequest);
        return createSuccessResponse(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить XxxYyy",
            description = "Мягкое удаление записи XxxYyy из справочника",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    )
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<BaseResponse> delete(
            @Parameter(description = "ID XxxYyy", example = "1") @PathVariable Long id,
            HttpServletRequest httpServletRequest) {
        service.delete(id, httpServletRequest);
        return createSuccessResponse(null);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить XxxYyy по ID",
            description = "Получение XxxYyy из справочника по идентификатору",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = XxxYyyResponse.class)))
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse> get(
            @Parameter(description = "ID XxxYyy", example = "1") @PathVariable Long id) {
        var response = service.get(id);
        return createSuccessResponse(response);
    }

    @PostMapping("/filter")
    @Operation(
            summary = "Получить отфильтрованные XxxYyy",
            description = "Фильтрация и постраничное получение списка XxxYyy из справочника",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = PageXxxYyyResponse.class))),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Фильтр для поиска XxxYyy",
                    content = @Content(schema = @Schema(implementation = XxxYyyFilter.class))
            )
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse> filter(@Valid @RequestBody XxxYyyFilter filter) {
        var response = service.findAllWithFilter(filter);
        return createSuccessResponse(response);
    }
}
```

## 6. DTO структура

### Request DTO (единый для create/update)

```java
package kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for XxxYyy operations.
 *
 * @author Bishkek Petroleum Development Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос для работы с XxxYyy")
public class XxxYyyRequest {

    @Schema(description = "ID записи (для обновления)", example = "1")
    private Long id;

    @NotBlank(message = "Russian name is required")
    @Size(max = 255, message = "Russian name must not exceed 255 characters")
    @Schema(description = "Название XxxYyy на русском языке", 
            example = "Пример названия")
    private String nameRu;

    @NotBlank(message = "Kyrgyz name is required")
    @Size(max = 255, message = "Kyrgyz name must not exceed 255 characters")
    @Schema(description = "Название XxxYyy на кыргызском языке", 
            example = "Мисал аты")
    private String nameKy;

    // Дополнительные поля специфичные для справочника
    @Schema(description = "Специфичное поле", example = "Значение")
    private String specificField;
}
```

### Response DTO

```java
package kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for XxxYyy.
 *
 * @author Bishkek Petroleum Development Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными XxxYyy")
public class XxxYyyResponse {

    @Schema(description = "Идентификатор", example = "1")
    private Long id;

    @Schema(description = "Алиас", example = "EXAMPLE_ALIAS")
    private String alias;

    @Schema(description = "Название на русском языке", example = "Пример названия")
    private String nameRu;

    @Schema(description = "Название на кыргызском языке", example = "Мисал аты")
    private String nameKy;

    @Schema(description = "Специфичное поле", example = "Значение")
    private String specificField;

    @Schema(description = "Дата создания")
    private LocalDateTime createdAt;

    @Schema(description = "Дата обновления")
    private LocalDateTime updatedAt;
}
```

### Filter DTO

```java
package kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import kg.bishkekpetroleum.salesmanagement.dto.BasePageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Filter DTO for XxxYyy handbook.
 *
 * @author Bishkek Petroleum Development Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Фильтр для поиска XxxYyy")
public class XxxYyyFilter extends BasePageRequest {

    @Schema(description = "Поисковый текст по названию XxxYyy",
            example = "Пример")
    private String searchText;

    @Schema(description = "Алиас XxxYyy для точного поиска",
            example = "EXAMPLE_ALIAS")
    private String alias;

    @Schema(description = "Название на русском языке",
            example = "Пример названия")
    private String nameRu;

    @Schema(description = "Название на кыргызском языке",
            example = "Мисал аты")
    private String nameKy;

    @Schema(description = "Специфичное поле для фильтрации",
            example = "Значение")
    private String specificField;
}
```

### PageResponse DTO

```java
package kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kg.bishkekpetroleum.salesmanagement.dto.BasePageResponse;

/**
 * Paginated response DTO for XxxYyy handbook.
 *
 * @author Bishkek Petroleum Development Team
 * @since 1.0.0
 */
@Schema(description = "Постраничный ответ со списком XxxYyy")
public class PageXxxYyyResponse extends BasePageResponse<XxxYyyResponse> {
}
```

## 7. Mapper (MapStruct)

```java
package kg.bishkekpetroleum.salesmanagement.mapper.hb;

import kg.bishkekpetroleum.salesmanagement.db.entity.hb.HBXxxYyy;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.request.XxxYyyRequest;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.response.XxxYyyResponse;
import org.mapstruct.*;

/**
 * Mapper for XxxYyy entity and DTOs.
 *
 * @author Bishkek Petroleum Development Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface XxxYyyMapper {

    /**
     * Maps request DTO to entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "alias", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    HBXxxYyy toEntity(XxxYyyRequest request);

    /**
     * Maps entity to response DTO.
     */
    XxxYyyResponse toResponse(HBXxxYyy entity);

    /**
     * Updates entity from request DTO.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "alias", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(XxxYyyRequest request, @MappingTarget HBXxxYyy entity);
}
```

## 8. Базовые компоненты системы

### BaseController методы

```java
/**
 * Создает успешный ответ с данными
 */
protected ResponseEntity<BaseResponse> createSuccessResponse(final Object data) {
    return ResponseEntity.ok(
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
protected ResponseEntity<BaseResponse> createErrorResponse(final Object message) {
    return ResponseEntity.badRequest().body(
            BaseResponse.builder()
                    .success(Constants.ERROR)
                    .msg(message)
                    .res(null)
                    .build()
    );
}

/**
 * Создает ответ с ошибкой и кастомным HTTP статусом
 */
protected ResponseEntity<BaseResponse> createErrorResponse(final Object message, final HttpStatus status) {
    return ResponseEntity.status(status).body(
            BaseResponse.builder()
                    .success(Constants.ERROR)
                    .msg(message)
                    .res(null)
                    .build()
    );
}

/**
 * Конвертация страницы из 1-based в 0-based для Spring Data
 */
public static int getPage(final Integer page) {
    return page != null && page > 0 ? page - 1 : 0;
}
```

### HandbookAliasUtils методы

```java
/**
 * Генерирует уникальный алиас для справочника на основе русского названия
 * 
 * @param nameRu Русское название для генерации алиаса
 * @param repository Репозиторий для проверки уникальности
 * @param excludeId ID для исключения из проверки уникальности (для обновлений)
 * @return Сгенерированный уникальный алиас в формате UPPER_SNAKE_CASE
 */
public <T extends BaseHandbook> String generateUniqueAlias(String nameRu,
                                                           JpaRepository<T, Long> repository,
                                                           Long excludeId);

/**
 * Генерирует базовый алиас без проверки уникальности
 * - Транслитерирует кириллицу в латиницу  
 * - Применяет аббревиатуры (менеджер -> manager, станция -> station)
 * - Исключает стоп-слова (и, в, на, для, с, по, из, к, от)
 * - Обрезает до 50 символов максимум
 * 
 * @param nameRu Русское название
 * @return Базовый алиас в формате UPPER_SNAKE_CASE
 */
public String generateBaseAlias(String nameRu);

/**
 * Проверяет корректность формата алиаса
 * 
 * @param alias Алиас для проверки
 * @return true если алиас корректен (UPPER_SNAKE_CASE, до 50 символов)
 */
public boolean isValidAlias(String alias);
```

**Примеры работы алиас-генератора:**
- "Региональный менеджер" → "REGIONAL_MANAGER"
- "Автозаправочная станция" → "STATION"
- "Линейное распределение" → "LINEAR_DISTRIBUTION"
- "Бензин АИ-95" → "BENZIN_AI_95"

### SysLogsRequestService методы

```java
/**
 * Сохраняет успешную операцию в БД для аудита
 * Используется во всех CUD операциях справочников
 */
void saveSuccessToDb(String className, String methodName, String message, HttpServletRequest request);

/**
 * Сохраняет успешную операцию в файл и БД
 */
void saveSuccessToFileAndDb(String nameClass, String nameFunction, String message, HttpServletRequest request);

/**
 * Сохраняет исключение в файл и БД
 */
void saveExceptionToFileAndDb(String nameClass, Exception e, String nameFunction, HttpServletRequest request);

/**
 * Сохраняет ошибку в файл и БД
 */
void saveErrorToFileAndDb(String nameClass, String errorMessage, String nameFunction, HttpServletRequest request);

/**
 * Асинхронно сохраняет лог в файл
 */
@Async
void saveToFile(String nameClass, String nameFunction, String message, LogLevel logLevel);
```

**Пример использования в справочниках:**
```java
// В методе создания
logService.saveSuccessToDb(
    this.getClass().getSimpleName(),
    Thread.currentThread().getStackTrace()[1].getMethodName(),
    "XxxYyy created: " + saved.getAlias(),
    httpServletRequest);
```

## 9. Спецификации для сложной фильтрации

### Паттерн создания Specification класса

```java
package kg.bishkekpetroleum.salesmanagement.db.repository.specification.hb;

import kg.bishkekpetroleum.salesmanagement.db.entity.hb.HBXxxYyy;
import kg.bishkekpetroleum.salesmanagement.dto.hb.xxxyyy.filter.XxxYyyFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Specification for filtering XxxYyy entities.
 *
 * @author Bishkek Petroleum Development Team
 * @since 1.0.0
 */
public class XxxYyySpecification {

    /**
     * Главный метод для построения спецификации на основе фильтра
     */
    public static Specification<HBXxxYyy> withFilter(XxxYyyFilter filter) {
        if (filter == null) {
            return Specification.where(null);
        }

        return Specification.where(aliasContains(filter.getAlias()))
                .and(nameRuContains(filter.getNameRu()))
                .and(nameKyContains(filter.getNameKy()))
                .and(searchTextContains(filter.getSearchText()))
                .and(deletedEquals(filter.getDeleted()))
                .and(createdAtBetween(filter.getCreatedAtFrom(), filter.getCreatedAtTo()));
    }

    /**
     * Фильтрация по алиасу (частичное совпадение, регистронезависимо)
     */
    private static Specification<HBXxxYyy> aliasContains(String alias) {
        return (root, query, criteriaBuilder) -> {
            if (alias == null || alias.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("alias")),
                    "%" + alias.toLowerCase() + "%"
            );
        };
    }

    /**
     * Фильтрация по русскому названию (частичное совпадение, регистронезависимо)
     */
    private static Specification<HBXxxYyy> nameRuContains(String nameRu) {
        return (root, query, criteriaBuilder) -> {
            if (nameRu == null || nameRu.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("nameRu")),
                    "%" + nameRu.toLowerCase() + "%"
            );
        };
    }

    /**
     * Фильтрация по кыргызскому названию (частичное совпадение, регистронезависимо)
     */
    private static Specification<HBXxxYyy> nameKyContains(String nameKy) {
        return (root, query, criteriaBuilder) -> {
            if (nameKy == null || nameKy.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("nameKy")),
                    "%" + nameKy.toLowerCase() + "%"
            );
        };
    }

    /**
     * Поиск по тексту (ищет в nameRu И nameKy)
     */
    private static Specification<HBXxxYyy> searchTextContains(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (searchText == null || searchText.trim().isEmpty()) {
                return null;
            }
            String likePattern = "%" + searchText.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("nameRu")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("nameKy")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("alias")), likePattern)
            );
        };
    }

    /**
     * Фильтрация по статусу удаления
     */
    private static Specification<HBXxxYyy> deletedEquals(Boolean deleted) {
        return (root, query, criteriaBuilder) -> {
            if (deleted == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("deleted"), deleted);
        };
    }

    /**
     * Фильтрация по диапазону дат создания
     */
    private static Specification<HBXxxYyy> createdAtBetween(String dateFrom, String dateTo) {
        return (root, query, criteriaBuilder) -> {
            if (dateFrom == null && dateTo == null) {
                return null;
            }

            if (dateFrom != null && dateTo != null) {
                return criteriaBuilder.between(root.get("createdAt"), 
                        LocalDate.parse(dateFrom).atStartOfDay(),
                        LocalDate.parse(dateTo).atTime(23, 59, 59));
            } else if (dateFrom != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), 
                        LocalDate.parse(dateFrom).atStartOfDay());
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), 
                        LocalDate.parse(dateTo).atTime(23, 59, 59));
            }
        };
    }
}
```

### Использование Specification в Service

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.stream.Collectors;

@Override
@Cacheable(value = "xxxYyyFilters")
public PageXxxYyyResponse findAllWithFilter(XxxYyyFilter filter) {
    // Построение спецификации
    Specification<HBXxxYyy> spec = XxxYyySpecification.withFilter(filter);
    
    // Построение Pageable
    Sort sort = Sort.by(Sort.Direction.ASC, "nameRu");
    PageRequest pageRequest = PageRequest.of(getPage(filter.getPage()), filter.getSize(), sort);
    
    // Выполнение запроса
    Page<HBXxxYyy> page = repository.findAll(spec, pageRequest);
    
    // Маппинг результатов
    List<XxxYyyResponse> content = page.getContent().stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    
    return BasePageResponse.of(content, page);
}
```

## 10. Flyway миграции для справочников

### Паттерн именования файлов миграций

```
V{номер}__HB_{Название_Справочника}.sql
```

**Примеры:**
- `V15__HB_Xxx_Yyy.sql`
- `V16__HB_Customer_Types.sql`
- `V17__HB_Fuel_Types.sql`

### Шаблон SQL миграции

```sql
-- =============================================================================
-- СПРАВОЧНИК XXX_YYY (V15__HB_Xxx_Yyy.sql)
-- Создание нового справочника XxxYyy для системы управления продажами АЗС
-- =============================================================================

-- Создание таблицы справочника
CREATE TABLE IF NOT EXISTS hb_xxx_yyy (
    id BIGSERIAL PRIMARY KEY,
    alias VARCHAR(50) NOT NULL UNIQUE,
    name_ru VARCHAR(255) NOT NULL,
    name_ky VARCHAR(255) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Дополнительные поля специфичные для справочника
    specific_field VARCHAR(255),
    
    -- Внешние ключи (если необходимы)
    parent_id BIGINT REFERENCES hb_parent_table(id)
);

-- Создание индексов
CREATE INDEX IF NOT EXISTS idx_hb_xxx_yyy_alias ON hb_xxx_yyy(alias);
CREATE INDEX IF NOT EXISTS idx_hb_xxx_yyy_name_ru ON hb_xxx_yyy(name_ru);
CREATE INDEX IF NOT EXISTS idx_hb_xxx_yyy_name_ky ON hb_xxx_yyy(name_ky);
CREATE INDEX IF NOT EXISTS idx_hb_xxx_yyy_deleted ON hb_xxx_yyy(deleted);
CREATE INDEX IF NOT EXISTS idx_hb_xxx_yyy_specific_field ON hb_xxx_yyy(specific_field);

-- Составные индексы для уникальности
CREATE UNIQUE INDEX IF NOT EXISTS idx_hb_xxx_yyy_names_deleted 
    ON hb_xxx_yyy(name_ru, name_ky, deleted) 
    WHERE deleted = FALSE;

-- Комментарии к таблице и колонкам
COMMENT ON TABLE hb_xxx_yyy IS 'Справочник XxxYyy для системы управления продажами АЗС';
COMMENT ON COLUMN hb_xxx_yyy.id IS 'Уникальный идентификатор записи';
COMMENT ON COLUMN hb_xxx_yyy.alias IS 'Уникальный алиас в формате UPPER_SNAKE_CASE';
COMMENT ON COLUMN hb_xxx_yyy.name_ru IS 'Наименование на русском языке';
COMMENT ON COLUMN hb_xxx_yyy.name_ky IS 'Наименование на кыргызском языке';
COMMENT ON COLUMN hb_xxx_yyy.deleted IS 'Флаг мягкого удаления';
COMMENT ON COLUMN hb_xxx_yyy.created_at IS 'Дата и время создания записи';
COMMENT ON COLUMN hb_xxx_yyy.updated_at IS 'Дата и время последнего обновления записи';
COMMENT ON COLUMN hb_xxx_yyy.specific_field IS 'Специфичное поле для данного справочника';

-- Вставка начальных данных (опционально)
INSERT INTO hb_xxx_yyy (alias, name_ru, name_ky, specific_field, created_at, updated_at) 
VALUES 
    ('EXAMPLE_1', 'Пример 1', 'Мисал 1', 'Значение 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('EXAMPLE_2', 'Пример 2', 'Мисал 2', 'Значение 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (alias) DO NOTHING;
```

### Ключевые требования к миграциям

1. **Обязательные поля**: `id`, `alias`, `name_ru`, `name_ky`, `deleted`, `created_at`, `updated_at`
2. **Уникальные ограничения**: `alias` должен быть уникальным
3. **Составные ограничения**: `name_ru + name_ky` уникальны для неудаленных записей
4. **Индексы**: На все поля поиска и внешние ключи
5. **Комментарии**: Обязательные для всех таблиц и важных колонок
6. **ON CONFLICT**: При вставке данных используй `ON CONFLICT ... DO NOTHING`

## Ключевые принципы

### 1. Наследование и переиспользование
- **Entities** наследуют от `BaseHandbook`
- **Repositories** наследуют от `BaseHandbookRepository<T>`
- **Максимально используй** базовые методы вместо дублирования кода

### 2. Единообразие naming conventions
- **Entity**: `HBXxxYyy` (Pascal case)
- **Repository**: `HBXxxYyyRepository`
- **Service**: `XxxYyyService` + `XxxYyyServiceImpl`
- **Controller**: `XxxYyyController`
- **DTO package**: `dto.hb.xxxyyy` (lower case)
- **REST endpoint**: `/api/v1/handbooks/xxx-yyy` (kebab-case)

### 3. Стандартные REST endpoints
- `POST /` - создание (ADMIN+)
- `PUT /` - обновление (ADMIN+)
- `DELETE /{id}` - мягкое удаление (ADMIN+)
- `GET /{id}` - получение по ID (AUTH)
- `POST /filter` - фильтрация с пагинацией (AUTH)

### 4. Безопасность
- **CUD операции**: `@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")`
- **Чтение**: `@PreAuthorize("isAuthenticated()")`

### 5. Кеширование
- **Service methods**: `@Cacheable`, `@CacheEvict`
- **Cache names**: `{entityName}s`, `{entityName}Filters`

### 6. Логирование и аудит
- **Все CUD операции** логируются через `SysLogsRequestService`
- **Debug логи** для входных параметров
- **Info логи** для успешных операций

### 7. Автоматическая генерация алиасов
- Используй `HandbookAliasUtils.generateUniqueAlias()`
- Алиас генерируется на основе русского названия
- Проверка уникальности автоматическая

### 8. Мягкое удаление (Soft Delete)
- Используй поле `deleted` из `BaseHandbook`
- Все запросы фильтруются по `deleted = false`
- Восстановление удаленных записей при создании с тем же именем

### 9. Стандарты локализации
- **Swagger описания** - НА РУССКОМ ЯЗЫКЕ с примерами на русском/кыргызском
- **Валидационные сообщения** - НА АНГЛИЙСКОМ ЯЗЫКЕ (стандарт проекта)
- **Поля nameRu/nameKy** - обязательные для всех справочников
- **Примеры значений** - на соответствующих языках

## Чек-лист при создании нового справочника

- [ ] Entity наследует от `BaseHandbook`
- [ ] Repository наследует от `BaseHandbookRepository<T>`
- [ ] Service Interface следует паттерну A (6 стандартных методов)
- [ ] Service Implementation использует базовые методы репозитория
- [ ] Controller имеет 5 стандартных endpoints
- [ ] DTO включает Request, Response, Filter, PageResponse
- [ ] Mapper использует MapStruct
- [ ] Настроено кеширование
- [ ] Добавлено логирование всех операций
- [ ] Использована автоматическая генерация алиасов
- [ ] Настроена безопасность (ADMIN+ для CUD, AUTH для чтения)
- [ ] Реализовано мягкое удаление
- [ ] Swagger документация полная на русском языке
- [ ] Валидационные сообщения на английском языке
- [ ] Создана Flyway миграция для БД
- [ ] Добавлен Specification класс для фильтрации
- [ ] Все базовые компоненты интегрированы (BaseController, HandbookAliasUtils, SysLogsRequestService)

## Примеры существующих справочников

Для референса смотри следующие реализованные справочники:
- `CustomerType` (новый паттерн)
- `ShopCategory` (новый паттерн)
- `ShopProduct` (новый паттерн)
- `FuelType` (обновленный паттерн)
- `OperatingHours` (обновленный паттерн)

Все они следуют данному паттерну и могут служить примерами для новых справочников.