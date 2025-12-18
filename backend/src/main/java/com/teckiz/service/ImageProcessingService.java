package com.teckiz.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    @Value("${file.upload.local-path:uploads}")
    private String localUploadPath;

    @Value("${image.thumbnail.width:300}")
    private int thumbnailWidth;

    @Value("${image.thumbnail.height:300}")
    private int thumbnailHeight;

    @Value("${image.thumbnail.quality:0.85}")
    private float thumbnailQuality;

    /**
     * Generate thumbnail from image file
     * @param imageFile Original image file
     * @param outputPath Path to save thumbnail (relative to uploads folder)
     * @return Path to generated thumbnail
     */
    public String generateThumbnail(MultipartFile imageFile, String outputPath) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty or null");
        }

        BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());
        if (originalImage == null) {
            throw new IOException("Unable to read image file");
        }

        // Generate thumbnail
        BufferedImage thumbnail = Scalr.resize(originalImage, Scalr.Method.QUALITY,
                Scalr.Mode.AUTOMATIC, thumbnailWidth, thumbnailHeight, Scalr.OP_ANTIALIAS);

        // Save thumbnail
        Path uploadDir = Paths.get(localUploadPath);
        Path thumbnailPath = uploadDir.resolve(outputPath);
        
        // Create parent directories if they don't exist
        if (thumbnailPath.getParent() != null && !Files.exists(thumbnailPath.getParent())) {
            Files.createDirectories(thumbnailPath.getParent());
        }

        // Determine format from original filename
        String format = getImageFormat(imageFile.getOriginalFilename());
        ImageIO.write(thumbnail, format, thumbnailPath.toFile());

        log.info("Thumbnail generated: {}", thumbnailPath);
        return "/uploads/" + outputPath;
    }

    /**
     * Generate thumbnail from image bytes
     */
    public byte[] generateThumbnailBytes(byte[] imageBytes, String format) throws IOException {
        BufferedImage originalImage = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
        if (originalImage == null) {
            throw new IOException("Unable to read image bytes");
        }

        BufferedImage thumbnail = Scalr.resize(originalImage, Scalr.Method.QUALITY,
                Scalr.Mode.AUTOMATIC, thumbnailWidth, thumbnailHeight, Scalr.OP_ANTIALIAS);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumbnail, format, baos);
        return baos.toByteArray();
    }

    /**
     * Resize image to specific dimensions
     */
    public BufferedImage resizeImage(BufferedImage image, int width, int height) {
        return Scalr.resize(image, Scalr.Method.QUALITY,
                Scalr.Mode.AUTOMATIC, width, height, Scalr.OP_ANTIALIAS);
    }

    /**
     * Resize image maintaining aspect ratio
     */
    public BufferedImage resizeImageMaintainAspect(BufferedImage image, int maxWidth, int maxHeight) {
        return Scalr.resize(image, Scalr.Method.QUALITY,
                Scalr.Mode.AUTOMATIC, maxWidth, maxHeight, Scalr.OP_ANTIALIAS);
    }

    /**
     * Crop image to specific dimensions
     */
    public BufferedImage cropImage(BufferedImage image, int x, int y, int width, int height) {
        return Scalr.crop(image, x, y, width, height);
    }

    /**
     * Crop image to center
     */
    public BufferedImage cropImageToCenter(BufferedImage image, int width, int height) {
        int x = (image.getWidth() - width) / 2;
        int y = (image.getHeight() - height) / 2;
        return cropImage(image, x, y, width, height);
    }

    /**
     * Get image dimensions
     */
    public ImageDimensions getImageDimensions(MultipartFile imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile.getInputStream());
        if (image == null) {
            throw new IOException("Unable to read image file");
        }
        return new ImageDimensions(image.getWidth(), image.getHeight());
    }

    /**
     * Get image dimensions from bytes
     */
    public ImageDimensions getImageDimensions(byte[] imageBytes) throws IOException {
        BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new IOException("Unable to read image bytes");
        }
        return new ImageDimensions(image.getWidth(), image.getHeight());
    }

    /**
     * Validate if file is an image
     */
    public boolean isImage(MultipartFile file) {
        if (file == null || file.getContentType() == null) {
            return false;
        }
        String contentType = file.getContentType();
        return contentType.startsWith("image/");
    }

    /**
     * Get image format from filename
     */
    private String getImageFormat(String filename) {
        if (filename == null) {
            return "jpg";
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return "png";
        } else if (lower.endsWith(".gif")) {
            return "gif";
        } else if (lower.endsWith(".bmp")) {
            return "bmp";
        } else {
            return "jpg"; // Default to JPG
        }
    }

    /**
     * Generate multiple thumbnail sizes
     */
    public ThumbnailSet generateThumbnailSet(MultipartFile imageFile, String basePath) throws IOException {
        BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());
        if (originalImage == null) {
            throw new IOException("Unable to read image file");
        }

        String format = getImageFormat(imageFile.getOriginalFilename());
        String baseName = basePath.substring(0, basePath.lastIndexOf('.'));
        String extension = basePath.substring(basePath.lastIndexOf('.'));

        // Small thumbnail (150x150)
        BufferedImage small = Scalr.resize(originalImage, Scalr.Method.QUALITY,
                Scalr.Mode.AUTOMATIC, 150, 150, Scalr.OP_ANTIALIAS);
        String smallPath = saveThumbnail(small, baseName + "_small" + extension, format);

        // Medium thumbnail (300x300)
        BufferedImage medium = Scalr.resize(originalImage, Scalr.Method.QUALITY,
                Scalr.Mode.AUTOMATIC, 300, 300, Scalr.OP_ANTIALIAS);
        String mediumPath = saveThumbnail(medium, baseName + "_medium" + extension, format);

        // Large thumbnail (600x600)
        BufferedImage large = Scalr.resize(originalImage, Scalr.Method.QUALITY,
                Scalr.Mode.AUTOMATIC, 600, 600, Scalr.OP_ANTIALIAS);
        String largePath = saveThumbnail(large, baseName + "_large" + extension, format);

        return new ThumbnailSet(smallPath, mediumPath, largePath);
    }

    private String saveThumbnail(BufferedImage thumbnail, String filename, String format) throws IOException {
        Path uploadDir = Paths.get(localUploadPath);
        Path thumbnailPath = uploadDir.resolve(filename);
        
        if (thumbnailPath.getParent() != null && !Files.exists(thumbnailPath.getParent())) {
            Files.createDirectories(thumbnailPath.getParent());
        }

        ImageIO.write(thumbnail, format, thumbnailPath.toFile());
        return "/uploads/" + filename;
    }

    /**
     * Image dimensions data class
     */
    public static class ImageDimensions {
        private final int width;
        private final int height;

        public ImageDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    /**
     * Thumbnail set data class
     */
    public static class ThumbnailSet {
        private final String small;
        private final String medium;
        private final String large;

        public ThumbnailSet(String small, String medium, String large) {
            this.small = small;
            this.medium = medium;
            this.large = large;
        }

        public String getSmall() {
            return small;
        }

        public String getMedium() {
            return medium;
        }

        public String getLarge() {
            return large;
        }
    }
}

