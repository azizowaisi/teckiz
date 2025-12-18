package com.teckiz.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {

    @Value("${app.aws.s3.bucket-name:teckiz-uploads}")
    private String bucketName;

    @Value("${app.aws.region:us-east-1}")
    private String region;

    @Value("${file.upload.local-path:uploads}")
    private String localUploadPath;

    @Value("${file.upload.use-s3:false}")
    private Boolean useS3;

    private final S3Client s3Client; // Optional - can be null if S3 not configured

    public FileUploadService(S3Client s3Client) {
        this.s3Client = s3Client; // Can be null if S3 not configured
    }

    /**
     * Upload file to S3 or local storage
     * @param file MultipartFile to upload
     * @param folder Folder path in bucket/storage (e.g., "images", "documents")
     * @return URL/path to uploaded file
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        String fileName = generateFileName(file.getOriginalFilename());
        String filePath = folder != null && !folder.isEmpty() 
            ? folder + "/" + fileName 
            : fileName;

        if (useS3 && s3Client != null) {
            return uploadToS3(file, filePath);
        } else {
            return uploadToLocal(file, filePath);
        }
    }

    /**
     * Upload file to S3
     */
    private String uploadToS3(MultipartFile file, String filePath) throws IOException {
        try {
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));

            String url = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                    bucketName, region, filePath);
            
            log.info("File uploaded to S3: {}", url);
            return url;

        } catch (S3Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Upload file to local storage
     */
    private String uploadToLocal(MultipartFile file, String filePath) throws IOException {
        try {
            Path uploadDir = Paths.get(localUploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path folderPath = uploadDir.resolve(filePath).getParent();
            if (folderPath != null && !Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            Path targetPath = uploadDir.resolve(filePath);
            Files.copy(file.getInputStream(), targetPath);

            String url = "/uploads/" + filePath;
            log.info("File uploaded locally: {}", targetPath);
            return url;

        } catch (IOException e) {
            log.error("Error uploading file locally: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Delete file from S3 or local storage
     */
    public void deleteFile(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        if (useS3 && s3Client != null) {
            deleteFromS3(filePath);
        } else {
            deleteFromLocal(filePath);
        }
    }

    /**
     * Delete file from S3
     */
    private void deleteFromS3(String filePath) {
        try {
            // Extract key from URL if full URL provided
            String key = filePath;
            if (filePath.contains("amazonaws.com/")) {
                key = filePath.substring(filePath.indexOf("amazonaws.com/") + 14);
            }

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());

            log.info("File deleted from S3: {}", key);
        } catch (S3Exception e) {
            log.error("Error deleting file from S3: {}", e.getMessage(), e);
        }
    }

    /**
     * Delete file from local storage
     */
    private void deleteFromLocal(String filePath) {
        try {
            // Remove /uploads/ prefix if present
            String relativePath = filePath.startsWith("/uploads/") 
                ? filePath.substring(9) 
                : filePath;

            Path targetPath = Paths.get(localUploadPath).resolve(relativePath);
            if (Files.exists(targetPath)) {
                Files.delete(targetPath);
                log.info("File deleted locally: {}", targetPath);
            }
        } catch (IOException e) {
            log.error("Error deleting file locally: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate unique file name
     */
    private String generateFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return UUID.randomUUID().toString();
        }

        String extension = "";
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot > 0) {
            extension = originalFilename.substring(lastDot);
        }

        String baseName = originalFilename.substring(0, lastDot > 0 ? lastDot : originalFilename.length());
        baseName = baseName.replaceAll("[^a-zA-Z0-9]", "-").toLowerCase();
        
        return baseName + "-" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    /**
     * Get file size in bytes
     */
    public long getFileSize(MultipartFile file) {
        return file != null ? file.getSize() : 0;
    }

    /**
     * Validate file type
     */
    public boolean isValidFileType(MultipartFile file, String[] allowedTypes) {
        if (file == null || file.getContentType() == null) {
            return false;
        }

        String contentType = file.getContentType();
        for (String allowedType : allowedTypes) {
            if (contentType.startsWith(allowedType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate file size
     */
    public boolean isValidFileSize(MultipartFile file, long maxSizeBytes) {
        return file != null && file.getSize() <= maxSizeBytes;
    }
}

