# MinIO File Storage - Руководство по реализации

## Обзор

MinIO - S3-совместимое объектное хранилище для управления файлами (фотографии, документы).

**Возможности:**
- Загрузка/скачивание файлов
- Presigned URLs для временного доступа
- Организация файлов по папкам (tickets/ID/...)
- Типизация файлов (INITIAL, RESULT)
- Валидация размера и типа файлов
- Base64 загрузка

---

## Архитектура

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Controller    │────▶│   FileService   │────▶│   MinioClient   │
│  MultipartFile  │     │   uploadFile()  │     │   putObject()   │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                       │                       │
        ▼                       ▼                       ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  TicketFile     │     │   Database      │     │   MinIO Server  │
│  Entity         │◀────│   file metadata │     │   bucket/files  │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

---

## 1. Зависимости (build.gradle)

```groovy
dependencies {
    // MinIO Java SDK
    implementation 'io.minio:minio:8.5.7'
}
```

---

## 2. Конфигурация

### application.properties

```properties
# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=100MB
spring.servlet.multipart.file-size-threshold=512KB

# MinIO Configuration
minio.endpoint=http://localhost:9000
minio.username=minioadmin
minio.password=minioadmin123
minio.secure=false
bucket.name=my-app-files
```

### MinioConfig.java

```java
package com.example.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация MinIO клиента
 */
@Configuration
@Slf4j
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.username}")
    private String username;

    @Value("${minio.password}")
    private String password;

    @Value("${minio.secure:false}")
    private boolean secure;

    @Bean
    public MinioClient minioClient() {
        log.info("Configuring MinIO client with endpoint: {}", endpoint);

        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(username, password)
                    .build();

            log.info("MinIO client configured successfully");
            return client;

        } catch (Exception e) {
            log.error("Failed to configure MinIO client: {}", e.getMessage(), e);
            throw new RuntimeException("MinIO configuration failed", e);
        }
    }
}
```

---

## 3. База данных

### Таблица для файлов

```sql
CREATE TABLE files (
    id                BIGSERIAL PRIMARY KEY,

    -- Связь с родительской сущностью
    entity_type       VARCHAR(50) NOT NULL,      -- 'TICKET', 'USER', 'PRODUCT'
    entity_id         BIGINT NOT NULL,

    -- Информация о файле
    original_filename VARCHAR(255) NOT NULL,     -- Оригинальное имя файла
    stored_filename   VARCHAR(255) NOT NULL,     -- UUID имя в хранилище
    file_path         VARCHAR(500) NOT NULL,     -- Полный путь: bucket/2025/01/29/tickets/1/uuid.jpg
    file_size         BIGINT,                    -- Размер в байтах
    content_type      VARCHAR(100),              -- MIME тип: image/jpeg

    -- Тип файла (опционально)
    file_type         VARCHAR(20) DEFAULT 'GENERAL',  -- INITIAL, RESULT, AVATAR, DOCUMENT

    -- Геолокация из EXIF (для фото)
    latitude          DOUBLE PRECISION,
    longitude         DOUBLE PRECISION,
    photo_taken_at    TIMESTAMP,

    -- Аудит
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by        BIGINT,
    deleted           BOOLEAN DEFAULT FALSE
);

-- Индексы
CREATE INDEX idx_files_entity ON files (entity_type, entity_id);
CREATE INDEX idx_files_type ON files (entity_id, file_type);
CREATE INDEX idx_files_deleted ON files (deleted);
```

### Пример: Таблица файлов для заявок

```sql
CREATE TABLE ticket_files (
    id                BIGSERIAL PRIMARY KEY,
    ticket_id         BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,

    original_filename VARCHAR(255) NOT NULL,
    stored_filename   VARCHAR(255) NOT NULL,
    file_path         VARCHAR(500) NOT NULL,
    file_size         BIGINT,
    content_type      VARCHAR(100),

    -- INITIAL = фото при создании, RESULT = фото результата
    file_type         VARCHAR(20) NOT NULL DEFAULT 'INITIAL',

    latitude          DOUBLE PRECISION,
    longitude         DOUBLE PRECISION,
    photo_taken_at    TIMESTAMP,

    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by        BIGINT,
    deleted           BOOLEAN DEFAULT FALSE,

    CONSTRAINT check_file_type CHECK (file_type IN ('INITIAL', 'RESULT'))
);

CREATE INDEX idx_ticket_files_ticket ON ticket_files(ticket_id);
CREATE INDEX idx_ticket_files_type ON ticket_files(ticket_id, file_type);
```

---

## 4. FileType Enum

```java
package com.example.db.enums;

/**
 * Тип файла
 */
public enum FileType {

    INITIAL("Начальные фотографии", "Фотографии при создании"),
    RESULT("Фотографии результата", "Фотографии выполненной работы"),
    DOCUMENT("Документ", "Прикрепленный документ"),
    AVATAR("Аватар", "Фотография профиля");

    private final String displayName;
    private final String description;

    FileType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
```

---

## 5. File Entity

```java
package com.example.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Файл, прикрепленный к сущности
 */
@Entity
@Table(name = "ticket_files")
@Getter
@Setter
@NoArgsConstructor
public class TicketFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 20)
    private FileType fileType = FileType.INITIAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    // Геолокация из EXIF
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "photo_taken_at")
    private LocalDateTime photoTakenAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "deleted")
    private Boolean deleted = false;

    // Конструктор для создания
    public TicketFile(String originalFilename, String storedFilename, String filePath,
                      Long fileSize, String contentType, Ticket ticket) {
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.fileType = FileType.INITIAL;
        this.ticket = ticket;
    }

    public TicketFile(String originalFilename, String storedFilename, String filePath,
                      Long fileSize, String contentType, FileType fileType, Ticket ticket) {
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.fileType = fileType;
        this.ticket = ticket;
    }

    public boolean hasGeoLocation() {
        return latitude != null && longitude != null;
    }

    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }
}
```

---

## 6. File Service Interface

```java
package com.example.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Интерфейс сервиса управления файлами
 */
public interface FileService {

    /**
     * Загрузка файла
     * @param file файл
     * @param bucketName имя bucket
     * @param path путь (например: "tickets/123")
     * @return URL загруженного файла
     */
    String uploadFile(MultipartFile file, String bucketName, String path);

    /**
     * Загрузка нескольких файлов
     */
    List<String> uploadFiles(List<MultipartFile> files, String bucketName, String path);

    /**
     * Скачивание файла
     * @return байты файла
     */
    byte[] downloadFile(String bucketName, String fileName);

    /**
     * Удаление файла
     */
    void deleteFile(String bucketName, String fileName);

    /**
     * Получение presigned URL для временного доступа
     * @param expiry время жизни URL в секундах
     */
    String getPresignedUrl(String bucketName, String fileName, int expiry);

    /**
     * Проверка существования файла
     */
    boolean fileExists(String bucketName, String fileName);

    /**
     * Список файлов по префиксу
     */
    List<String> listFiles(String bucketName, String prefix);

    /**
     * Создание bucket если не существует
     */
    void createBucketIfNotExists(String bucketName);

    /**
     * Загрузка файла из Base64
     */
    String uploadBase64File(String base64Data, String fileName, String bucketName, String path);
}
```

---

## 7. File Service Implementation

```java
package com.example.service.impl;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import com.example.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${bucket.name:app-files}")
    private String defaultBucket;

    // Ограничения
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "txt"
    };

    @Override
    public String uploadFile(MultipartFile file, String bucketName, String path) {
        try {
            validateFile(file);
            createBucketIfNotExists(bucketName);

            String fileName = generateFileName(file.getOriginalFilename(), path);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("File uploaded: {} to bucket: {}", fileName, bucketName);
            return buildFileUrl(bucketName, fileName);

        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка загрузки файла", e);
        }
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files, String bucketName, String path) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFile(file, bucketName, path));
        }
        return urls;
    }

    @Override
    public byte[] downloadFile(String bucketName, String fileName) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            return stream.readAllBytes();

        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage(), e);
            throw new RuntimeException("Файл не найден", e);
        }
    }

    @Override
    public void deleteFile(String bucketName, String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("File deleted: {} from bucket: {}", fileName, bucketName);

        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка удаления файла", e);
        }
    }

    @Override
    public String getPresignedUrl(String bucketName, String fileName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(expiry)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка генерации URL", e);
        }
    }

    @Override
    public boolean fileExists(String bucketName, String fileName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> listFiles(String bucketName, String prefix) {
        List<String> fileNames = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .build()
            );
            for (Result<Item> result : results) {
                fileNames.add(result.get().objectName());
            }
        } catch (Exception e) {
            log.error("Error listing files: {}", e.getMessage(), e);
        }
        return fileNames;
    }

    @Override
    public void createBucketIfNotExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Created bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Error creating bucket: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка создания bucket", e);
        }
    }

    @Override
    public String uploadBase64File(String base64Data, String fileName, String bucketName, String path) {
        try {
            createBucketIfNotExists(bucketName);

            // Парсим Base64
            String actualBase64;
            String contentType = "image/jpeg";

            if (base64Data.startsWith("data:")) {
                String[] parts = base64Data.split(",");
                if (parts.length == 2) {
                    String header = parts[0];
                    actualBase64 = parts[1];

                    if (header.contains("image/png")) {
                        contentType = "image/png";
                        if (!fileName.endsWith(".png")) fileName += ".png";
                    } else if (header.contains("image/gif")) {
                        contentType = "image/gif";
                        if (!fileName.endsWith(".gif")) fileName += ".gif";
                    } else {
                        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
                            fileName += ".jpg";
                        }
                    }
                } else {
                    actualBase64 = base64Data;
                }
            } else {
                actualBase64 = base64Data;
                if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
                    fileName += ".jpg";
                }
            }

            byte[] fileData = Base64.getDecoder().decode(actualBase64);

            if (fileData.length > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("Файл слишком большой");
            }

            String fullFileName = generateFileName(fileName, path);

            try (InputStream inputStream = new ByteArrayInputStream(fileData)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fullFileName)
                                .stream(inputStream, fileData.length, -1)
                                .contentType(contentType)
                                .build()
                );
            }

            return buildFileUrl(bucketName, fullFileName);

        } catch (Exception e) {
            log.error("Error uploading base64 file: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка загрузки файла", e);
        }
    }

    // ===== PRIVATE METHODS =====

    /**
     * Валидация файла
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Файл слишком большой (макс. 10MB)");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Неверное имя файла");
        }

        String extension = getFileExtension(fileName).toLowerCase();
        boolean allowed = Arrays.asList(ALLOWED_EXTENSIONS).contains(extension);
        if (!allowed) {
            throw new IllegalArgumentException("Тип файла не разрешен");
        }
    }

    /**
     * Генерация уникального имени файла
     * Результат: 2025/01/29/tickets/123/uuid.jpg
     */
    private String generateFileName(String originalFileName, String path) {
        String extension = getFileExtension(originalFileName);
        String datePrefix = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String uniqueId = UUID.randomUUID().toString();

        StringBuilder fileName = new StringBuilder();
        fileName.append(datePrefix).append("/");

        if (path != null && !path.trim().isEmpty()) {
            fileName.append(path.replaceAll("^/+|/+$", "")).append("/");
        }

        fileName.append(uniqueId).append(".").append(extension);
        return fileName.toString();
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot == -1 ? "" : fileName.substring(lastDot + 1);
    }

    private String buildFileUrl(String bucketName, String fileName) {
        return minioEndpoint + "/" + bucketName + "/" + fileName;
    }
}
```

---

## 8. File Repository

```java
package com.example.db.repository;

import com.example.db.entity.TicketFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketFileRepository extends JpaRepository<TicketFile, Long> {

    List<TicketFile> findByTicketId(Long ticketId);

    List<TicketFile> findByTicketIdAndFileType(Long ticketId, FileType fileType);

    void deleteByTicketId(Long ticketId);
}
```

---

## 9. REST Controller

```java
package com.example.controller;

import com.example.dto.common.BaseResponse;
import com.example.dto.ticket.request.CreateTicketRequest;
import com.example.dto.ticket.response.TicketResponse;
import com.example.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * Создание заявки с фотографиями
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Создание заявки с фото")
    public ResponseEntity<BaseResponse<TicketResponse>> createTicket(
            @RequestPart("request") CreateTicketRequest request,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        TicketResponse ticket = ticketService.createTicket(request, files);
        return ResponseEntity.ok(BaseResponse.<TicketResponse>builder()
                .success(true)
                .result(ticket)
                .build());
    }

    /**
     * Завершение заявки с фотографиями результата
     */
    @PutMapping(value = "/{id}/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Завершить заявку с фото результата")
    public ResponseEntity<BaseResponse<TicketResponse>> completeTicket(
            @PathVariable Long id,
            @RequestPart(value = "request", required = false) CompleteTicketRequest request,
            @RequestParam(value = "resultFiles", required = false) List<MultipartFile> resultFiles) {

        TicketResponse ticket = ticketService.completeTicket(id, request, resultFiles);
        return ResponseEntity.ok(BaseResponse.<TicketResponse>builder()
                .success(true)
                .result(ticket)
                .build());
    }

    /**
     * Просмотр файла (inline)
     */
    @GetMapping("/{ticketId}/files/{fileId}/view")
    @Operation(summary = "Просмотр файла")
    public ResponseEntity<Resource> viewFile(
            @PathVariable Long ticketId,
            @PathVariable Long fileId) {

        return ticketService.viewTicketFile(ticketId, fileId);
    }

    /**
     * Скачивание файла (attachment)
     */
    @GetMapping("/{ticketId}/files/{fileId}/download")
    @Operation(summary = "Скачивание файла")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long ticketId,
            @PathVariable Long fileId) {

        return ticketService.downloadTicketFile(ticketId, fileId);
    }

    /**
     * Список файлов заявки
     */
    @GetMapping("/{ticketId}/files")
    @Operation(summary = "Список файлов заявки")
    public ResponseEntity<BaseResponse<List<FileInfo>>> getFiles(
            @PathVariable Long ticketId) {

        List<FileInfo> files = ticketService.getTicketFiles(ticketId);
        return ResponseEntity.ok(BaseResponse.<List<FileInfo>>builder()
                .success(true)
                .result(files)
                .build());
    }
}
```

---

## 10. Service Implementation (файлы в заявках)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketFileRepository ticketFileRepository;
    private final FileService fileService;

    @Value("${bucket.name}")
    private String bucketName;

    @Override
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, List<MultipartFile> photos) {
        // 1. Создаем заявку
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        // ... другие поля ...

        Ticket savedTicket = ticketRepository.save(ticket);
        ticketRepository.flush();

        // 2. Загружаем файлы
        if (photos != null && !photos.isEmpty()) {
            List<TicketFile> filesToSave = new ArrayList<>();

            for (MultipartFile photo : photos) {
                try {
                    String ticketPath = "tickets/" + savedTicket.getId();
                    String fileUrl = fileService.uploadFile(photo, bucketName, ticketPath);

                    TicketFile ticketFile = new TicketFile(
                            photo.getOriginalFilename(),
                            extractFileName(fileUrl),
                            extractFullPath(fileUrl, bucketName),
                            photo.getSize(),
                            photo.getContentType(),
                            savedTicket
                    );

                    filesToSave.add(ticketFile);

                } catch (Exception e) {
                    log.error("Failed to upload photo: {}", photo.getOriginalFilename(), e);
                }
            }

            if (!filesToSave.isEmpty()) {
                ticketFileRepository.saveAll(filesToSave);
            }
        }

        return mapToResponse(savedTicket);
    }

    @Override
    @Transactional
    public TicketResponse completeTicket(Long ticketId, CompleteTicketRequest request,
                                          List<MultipartFile> resultFiles) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        // Обновляем статус
        ticket.setStatus(TicketStatus.COMPLETED);
        ticket.setCompletedAt(LocalDateTime.now());

        if (request != null && request.getNotes() != null) {
            ticket.setTechnicianNotes(request.getNotes());
        }

        // Загружаем фото результата
        if (resultFiles != null && !resultFiles.isEmpty()) {
            List<TicketFile> filesToSave = new ArrayList<>();

            for (MultipartFile file : resultFiles) {
                try {
                    String ticketPath = "tickets/" + ticketId + "/results";
                    String fileUrl = fileService.uploadFile(file, bucketName, ticketPath);

                    TicketFile ticketFile = new TicketFile(
                            file.getOriginalFilename(),
                            extractFileName(fileUrl),
                            extractFullPath(fileUrl, bucketName),
                            file.getSize(),
                            file.getContentType(),
                            FileType.RESULT,  // <-- ВАЖНО: тип RESULT
                            ticket
                    );

                    filesToSave.add(ticketFile);

                } catch (Exception e) {
                    log.error("Failed to upload result file: {}", file.getOriginalFilename(), e);
                }
            }

            if (!filesToSave.isEmpty()) {
                ticketFileRepository.saveAll(filesToSave);
            }
        }

        ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> viewTicketFile(Long ticketId, Long fileId) {
        TicketFile ticketFile = ticketFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));

        // Проверяем принадлежность к заявке
        if (!ticketFile.getTicket().getId().equals(ticketId)) {
            throw new RuntimeException("Файл не принадлежит заявке");
        }

        // Извлекаем bucket и path
        String filePath = ticketFile.getFilePath();
        String[] parts = filePath.split("/", 2);
        String bucket = parts[0];
        String fileName = parts[1];

        byte[] fileData = fileService.downloadFile(bucket, fileName);

        ByteArrayResource resource = new ByteArrayResource(fileData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename(ticketFile.getOriginalFilename(), StandardCharsets.UTF_8)
                        .build()
        );
        headers.setContentType(MediaType.parseMediaType(ticketFile.getContentType()));
        headers.setContentLength(fileData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadTicketFile(Long ticketId, Long fileId) {
        TicketFile ticketFile = ticketFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));

        if (!ticketFile.getTicket().getId().equals(ticketId)) {
            throw new RuntimeException("Файл не принадлежит заявке");
        }

        String filePath = ticketFile.getFilePath();
        String[] parts = filePath.split("/", 2);
        String bucket = parts[0];
        String fileName = parts[1];

        byte[] fileData = fileService.downloadFile(bucket, fileName);

        ByteArrayResource resource = new ByteArrayResource(fileData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()  // <-- attachment для скачивания
                        .filename(ticketFile.getOriginalFilename(), StandardCharsets.UTF_8)
                        .build()
        );
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(fileData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    // Утилиты
    private String extractFileName(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
    }

    private String extractFullPath(String fileUrl, String bucket) {
        String bucketPath = "/" + bucket + "/";
        int index = fileUrl.indexOf(bucketPath);
        if (index != -1) {
            return fileUrl.substring(index + 1);
        }
        return bucket + "/" + extractFileName(fileUrl);
    }
}
```

---

## 11. Структура хранения в MinIO

```
bucket-name/
├── 2025/
│   └── 01/
│       └── 29/
│           └── tickets/
│               ├── 1/
│               │   ├── a1b2c3d4-uuid.jpg      (INITIAL)
│               │   ├── e5f6g7h8-uuid.jpg      (INITIAL)
│               │   └── results/
│               │       ├── i9j0k1l2-uuid.jpg  (RESULT)
│               │       └── m3n4o5p6-uuid.jpg  (RESULT)
│               ├── 2/
│               │   └── ...
│               └── ...
```

**В базе данных:**
```
file_path: bucket-name/2025/01/29/tickets/1/a1b2c3d4-uuid.jpg
stored_filename: a1b2c3d4-uuid.jpg
original_filename: photo_problem.jpg
file_type: INITIAL
```

---

## 12. Чек-лист для внедрения

- [ ] Добавить зависимость MinIO в build.gradle
- [ ] Настроить application.properties
- [ ] Создать MinioConfig
- [ ] Создать миграцию для таблицы файлов
- [ ] Создать FileType enum (если нужны типы)
- [ ] Создать File Entity
- [ ] Создать FileRepository
- [ ] Создать FileService interface
- [ ] Создать FileServiceImpl
- [ ] Добавить endpoints в Controller
- [ ] Протестировать загрузку/скачивание
- [ ] Настроить MinIO сервер (Docker или standalone)

---

## 13. Docker Compose для MinIO

```yaml
version: '3.8'

services:
  minio:
    image: minio/minio:latest
    container_name: minio
    ports:
      - "9000:9000"   # API
      - "9001:9001"   # Console
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin123
    volumes:
      - minio_data:/data
    command: server /data --console-address ":9001"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3

volumes:
  minio_data:
```

**Доступ:**
- API: http://localhost:9000
- Console: http://localhost:9001 (login: minioadmin / minioadmin123)
