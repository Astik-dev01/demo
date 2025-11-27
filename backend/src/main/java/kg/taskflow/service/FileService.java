package kg.taskflow.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service for file operations with MinIO
 */
public interface FileService {

    /**
     * Upload a file
     * @param file the file to upload
     * @param path optional path prefix (e.g., "tasks/123", "avatars")
     * @return the URL of the uploaded file
     */
    String uploadFile(MultipartFile file, String path);

    /**
     * Upload multiple files
     */
    List<String> uploadFiles(List<MultipartFile> files, String path);

    /**
     * Upload a file from Base64 data
     */
    String uploadBase64File(String base64Data, String fileName, String path);

    /**
     * Download a file as bytes
     */
    byte[] downloadFile(String filePath);

    /**
     * Delete a file
     */
    void deleteFile(String filePath);

    /**
     * Get a presigned URL for temporary access
     * @param filePath the file path
     * @param expirySeconds URL expiry in seconds
     */
    String getPresignedUrl(String filePath, int expirySeconds);

    /**
     * Check if a file exists
     */
    boolean fileExists(String filePath);

    /**
     * List files by prefix
     */
    List<String> listFiles(String prefix);

    /**
     * Get the public URL for a file
     */
    String getPublicUrl(String filePath);
}
