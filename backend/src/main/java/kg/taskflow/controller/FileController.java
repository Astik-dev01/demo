package kg.taskflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kg.taskflow.config.MinioConfig;
import kg.taskflow.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File upload and management")
public class FileController {

    private final FileService fileService;
    private final MinioConfig minioConfig;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a single file")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "path", required = false) String path) {

        String filePath = fileService.uploadFile(file, path);
        String publicUrl = fileService.getPublicUrl(filePath);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("filePath", filePath);
        response.put("url", publicUrl);
        response.put("originalName", file.getOriginalFilename());
        response.put("size", file.getSize());
        response.put("contentType", file.getContentType());

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple files")
    public ResponseEntity<Map<String, Object>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "path", required = false) String path) {

        List<String> filePaths = fileService.uploadFiles(files, path);
        List<String> urls = filePaths.stream()
                .map(fileService::getPublicUrl)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", filePaths.size());
        response.put("filePaths", filePaths);
        response.put("urls", urls);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/base64")
    @Operation(summary = "Upload a file from Base64 data")
    public ResponseEntity<Map<String, Object>> uploadBase64(
            @RequestBody Map<String, String> request) {

        String base64Data = request.get("data");
        String fileName = request.getOrDefault("fileName", "file");
        String path = request.get("path");

        String filePath = fileService.uploadBase64File(base64Data, fileName, path);
        String publicUrl = fileService.getPublicUrl(filePath);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("filePath", filePath);
        response.put("url", publicUrl);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/**")
    @Operation(summary = "Download a file")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam("path") String filePath) {

        byte[] fileData = fileService.downloadFile(filePath);
        ByteArrayResource resource = new ByteArrayResource(fileData);

        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
        );
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(fileData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @GetMapping("/view")
    @Operation(summary = "View a file inline (for images)")
    public ResponseEntity<Resource> viewFile(
            @RequestParam("path") String filePath) {

        byte[] fileData = fileService.downloadFile(filePath);
        ByteArrayResource resource = new ByteArrayResource(fileData);

        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        String contentType = guessContentType(fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
        );
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(fileData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @GetMapping("/presigned-url")
    @Operation(summary = "Get a presigned URL for temporary access")
    public ResponseEntity<Map<String, Object>> getPresignedUrl(
            @RequestParam("path") String filePath,
            @RequestParam(value = "expiry", defaultValue = "3600") int expirySeconds) {

        String presignedUrl = fileService.getPresignedUrl(filePath, expirySeconds);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("url", presignedUrl);
        response.put("expiresIn", expirySeconds);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Operation(summary = "Delete a file")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @RequestParam("path") String filePath) {

        fileService.deleteFile(filePath);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "File deleted successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    @Operation(summary = "List files by prefix")
    public ResponseEntity<Map<String, Object>> listFiles(
            @RequestParam(value = "prefix", defaultValue = "") String prefix) {

        List<String> files = fileService.listFiles(prefix);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", files.size());
        response.put("files", files);

        return ResponseEntity.ok(response);
    }

    private String guessContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".csv")) return "text/csv";
        return "application/octet-stream";
    }
}
