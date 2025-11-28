package kg.taskflow.service.impl;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import kg.taskflow.config.MinioConfig;
import kg.taskflow.exception.BadRequestException;
import kg.taskflow.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "svg",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "zip", "rar"
    );

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "svg"
    );

    @Override
    public String uploadFile(MultipartFile file, String path) {
        validateFile(file);

        try {
            String fileName = generateFileName(file.getOriginalFilename(), path);
            String bucket = minioConfig.getBucket();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("File uploaded: {} to bucket: {}", fileName, bucket);
            return fileName;

        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files, String path) {
        List<String> fileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            fileNames.add(uploadFile(file, path));
        }
        return fileNames;
    }

    @Override
    public String uploadBase64File(String base64Data, String fileName, String path) {
        try {
            String actualBase64;
            String contentType = "application/octet-stream";

            if (base64Data.startsWith("data:")) {
                String[] parts = base64Data.split(",");
                if (parts.length == 2) {
                    String header = parts[0];
                    actualBase64 = parts[1];

                    if (header.contains("image/png")) {
                        contentType = "image/png";
                        if (!fileName.toLowerCase().endsWith(".png")) fileName += ".png";
                    } else if (header.contains("image/gif")) {
                        contentType = "image/gif";
                        if (!fileName.toLowerCase().endsWith(".gif")) fileName += ".gif";
                    } else if (header.contains("image/jpeg") || header.contains("image/jpg")) {
                        contentType = "image/jpeg";
                        if (!fileName.toLowerCase().endsWith(".jpg") && !fileName.toLowerCase().endsWith(".jpeg")) {
                            fileName += ".jpg";
                        }
                    } else if (header.contains("image/webp")) {
                        contentType = "image/webp";
                        if (!fileName.toLowerCase().endsWith(".webp")) fileName += ".webp";
                    }
                } else {
                    actualBase64 = base64Data;
                }
            } else {
                actualBase64 = base64Data;
            }

            byte[] fileData = Base64.getDecoder().decode(actualBase64);

            if (fileData.length > MAX_FILE_SIZE) {
                throw new BadRequestException("File too large. Max size: 50MB");
            }

            String fullFileName = generateFileName(fileName, path);
            String bucket = minioConfig.getBucket();

            try (InputStream inputStream = new ByteArrayInputStream(fileData)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(fullFileName)
                                .stream(inputStream, fileData.length, -1)
                                .contentType(contentType)
                                .build()
                );
            }

            log.info("Base64 file uploaded: {} to bucket: {}", fullFileName, bucket);
            return fullFileName;

        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid Base64 data");
        } catch (Exception e) {
            log.error("Error uploading base64 file: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public byte[] downloadFile(String filePath) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioConfig.getBucket())
                        .object(filePath)
                        .build()
        )) {
            return stream.readAllBytes();
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage(), e);
            throw new BadRequestException("File not found: " + filePath);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(filePath)
                            .build()
            );
            log.info("File deleted: {}", filePath);

        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public String getPresignedUrl(String filePath, int expirySeconds) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucket())
                            .object(filePath)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to generate URL: " + e.getMessage());
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(filePath)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> listFiles(String prefix) {
        List<String> fileNames = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .prefix(prefix)
                            .recursive(true)
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
    public String getPublicUrl(String filePath) {
        return minioConfig.getFileUrl(filePath);
    }

    // ===== PRIVATE METHODS =====

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File too large. Max size: 50MB");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new BadRequestException("Invalid file name");
        }

        String extension = getFileExtension(fileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("File type not allowed: " + extension);
        }
    }

    private String generateFileName(String originalFileName, String path) {
        String extension = getFileExtension(originalFileName);
        String datePrefix = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String uniqueId = UUID.randomUUID().toString();

        StringBuilder fileName = new StringBuilder();
        fileName.append(datePrefix).append("/");

        if (path != null && !path.trim().isEmpty()) {
            fileName.append(path.replaceAll("^/+|/+$", "")).append("/");
        }

        fileName.append(uniqueId);
        if (!extension.isEmpty()) {
            fileName.append(".").append(extension);
        }

        return fileName.toString();
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot == -1 ? "" : fileName.substring(lastDot + 1).toLowerCase();
    }

    public boolean isImage(String fileName) {
        String extension = getFileExtension(fileName);
        return IMAGE_EXTENSIONS.contains(extension);
    }
}
