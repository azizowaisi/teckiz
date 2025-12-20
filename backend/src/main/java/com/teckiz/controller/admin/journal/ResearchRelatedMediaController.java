package com.teckiz.controller.admin.journal;

import com.teckiz.entity.Company;
import com.teckiz.entity.ResearchArticle;
import com.teckiz.entity.ResearchJournal;
import com.teckiz.entity.ResearchJournalVolume;
import com.teckiz.entity.ResearchRelatedMedia;
import com.teckiz.repository.ResearchArticleRepository;
import com.teckiz.repository.ResearchJournalRepository;
import com.teckiz.repository.ResearchJournalVolumeRepository;
import com.teckiz.repository.ResearchRelatedMediaRepository;
import com.teckiz.service.FileUploadService;
import com.teckiz.service.ImageProcessingService;
import com.teckiz.service.ModuleAccessManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/journal/admin/media")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class ResearchRelatedMediaController {

    private final ModuleAccessManager moduleAccessManager;
    private final ResearchRelatedMediaRepository mediaRepository;
    private final ResearchArticleRepository articleRepository;
    private final ResearchJournalRepository journalRepository;
    private final ResearchJournalVolumeRepository volumeRepository;
    private final FileUploadService fileUploadService;
    private final ImageProcessingService imageProcessingService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listMedia(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String mediaType,
            @RequestParam(required = false) String articleKey,
            @RequestParam(required = false) String journalKey,
            @RequestParam(required = false) String volumeKey,
            @RequestParam(required = false) Boolean poster) {

        Company company = moduleAccessManager.authenticateModule().getCompany();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<ResearchRelatedMedia> mediaList;

        List<ResearchRelatedMedia> mediaList;
        if (articleKey != null && !articleKey.isEmpty()) {
            mediaList = articleRepository.findByArticleKey(articleKey)
                    .map(article -> mediaRepository.findByResearchArticle(article))
                    .orElse(mediaRepository.findByCompany(company));
        } else if (journalKey != null && !journalKey.isEmpty()) {
            mediaList = journalRepository.findByJournalKey(journalKey)
                    .map(journal -> mediaRepository.findByResearchJournal(journal))
                    .orElse(mediaRepository.findByCompany(company));
        } else if (volumeKey != null && !volumeKey.isEmpty()) {
            mediaList = volumeRepository.findByVolumeKey(volumeKey)
                    .map(volume -> mediaRepository.findByResearchJournalVolume(volume))
                    .orElse(mediaRepository.findByCompany(company));
        } else {
            mediaList = mediaRepository.findByCompany(company);
        }

        // Filter by media type if provided
        if (mediaType != null && !mediaType.isEmpty()) {
            mediaList = mediaList.stream()
                    .filter(media -> mediaType.equals(media.getMediaType()))
                    .collect(Collectors.toList());
        }

        // Filter by poster if provided
        if (poster != null && poster) {
            mediaList = mediaList.stream()
                    .filter(ResearchRelatedMedia::getPoster)
                    .collect(Collectors.toList());
        }

        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, mediaList.size());
        List<ResearchRelatedMedia> paginated = mediaList.subList(start, end);

        List<Map<String, Object>> mediaResponses = paginated.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("media", mediaResponses);
        response.put("totalPages", (int) Math.ceil((double) mediaList.size() / size));
        response.put("totalElements", mediaList.size());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{mediaKey}")
    public ResponseEntity<Map<String, Object>> getMedia(@PathVariable String mediaKey) {
        moduleAccessManager.authenticateModule();

        return mediaRepository.findByRelatedMediaKey(mediaKey)
                .map(media -> ResponseEntity.ok(mapToResponse(media)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMedia(@RequestBody Map<String, Object> request) {
        Company company = moduleAccessManager.authenticateModule().getCompany();

        String location = (String) request.get("location");
        if (location == null || location.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Location is required"));
        }

        ResearchRelatedMedia.ResearchRelatedMediaBuilder builder = ResearchRelatedMedia.builder()
                .company(company)
                .location(location)
                .mimeType((String) request.get("mimeType"))
                .mediaType((String) request.get("mediaType"))
                .poster(request.get("poster") != null ? (Boolean) request.get("poster") : false)
                .fileName((String) request.get("fileName"))
                .fileSize(request.get("fileSize") != null ? ((Number) request.get("fileSize")).longValue() : null);

        // Set associations if provided
        if (request.get("articleKey") != null) {
            articleRepository.findByArticleKey((String) request.get("articleKey"))
                    .ifPresent(builder::researchArticle);
        }
        if (request.get("journalKey") != null) {
            journalRepository.findByJournalKey((String) request.get("journalKey"))
                    .ifPresent(builder::researchJournal);
        }
        if (request.get("volumeKey") != null) {
            volumeRepository.findByVolumeKey((String) request.get("volumeKey"))
                    .ifPresent(builder::researchJournalVolume);
        }

        ResearchRelatedMedia media = builder.build();
        media = mediaRepository.save(media);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Media created successfully", "mediaKey", media.getRelatedMediaKey()));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String articleKey,
            @RequestParam(required = false) String journalKey,
            @RequestParam(required = false) String volumeKey) {

        Company company = moduleAccessManager.authenticateModule().getCompany();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty"));
        }

        String location;
        String thumbnailLocation = null;
        try {
            location = fileUploadService.uploadFile(file, "research-media");

            // Generate thumbnail if it's an image
            if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
                try {
                    String thumbnailPath = "research-media/thumbnails/" + file.getOriginalFilename();
                    thumbnailLocation = imageProcessingService.generateThumbnail(file, thumbnailPath);
                } catch (IOException e) {
                    System.out.println("Failed to generate thumbnail: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }

        String fileName = file.getOriginalFilename();
        String mimeType = file.getContentType();

        ResearchRelatedMedia.ResearchRelatedMediaBuilder builder = ResearchRelatedMedia.builder()
                .company(company)
                .location(location)
                .mimeType(mimeType)
                .mediaType(determineMediaType(mimeType))
                .poster(false)
                .fileName(fileName)
                .fileSize(file.getSize());

        // Set associations if provided
        if (articleKey != null && !articleKey.isEmpty()) {
            articleRepository.findByArticleKey(articleKey)
                    .ifPresent(builder::researchArticle);
        }
        if (journalKey != null && !journalKey.isEmpty()) {
            journalRepository.findByJournalKey(journalKey)
                    .ifPresent(builder::researchJournal);
        }
        if (volumeKey != null && !volumeKey.isEmpty()) {
            volumeRepository.findByVolumeKey(volumeKey)
                    .ifPresent(builder::researchJournalVolume);
        }

        ResearchRelatedMedia media = builder.build();
        media = mediaRepository.save(media);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "File uploaded successfully",
                        "mediaKey", media.getRelatedMediaKey(),
                        "location", location,
                        "thumbnailLocation", thumbnailLocation
                ));
    }

    @PutMapping("/{mediaKey}")
    public ResponseEntity<?> updateMedia(
            @PathVariable String mediaKey,
            @RequestBody Map<String, Object> request) {

        Company company = moduleAccessManager.authenticateModule().getCompany();

        ResearchRelatedMedia media = mediaRepository.findByRelatedMediaKey(mediaKey)
                .orElseThrow(() -> new RuntimeException("Media not found"));

        if (!media.getCompany().getId().equals(company.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("location") != null) {
            media.setLocation((String) request.get("location"));
        }
        if (request.get("mimeType") != null) {
            media.setMimeType((String) request.get("mimeType"));
        }
        if (request.get("mediaType") != null) {
            media.setMediaType((String) request.get("mediaType"));
        }
        if (request.get("poster") != null) {
            media.setPoster((Boolean) request.get("poster"));
        }
        if (request.get("fileName") != null) {
            media.setFileName((String) request.get("fileName"));
        }
        if (request.get("fileSize") != null) {
            media.setFileSize(((Number) request.get("fileSize")).longValue());
        }

        media = mediaRepository.save(media);

        return ResponseEntity.ok(Map.of("message", "Media updated successfully"));
    }

    @DeleteMapping("/{mediaKey}")
    public ResponseEntity<?> deleteMedia(@PathVariable String mediaKey) {
        Company company = moduleAccessManager.authenticateModule().getCompany();

        ResearchRelatedMedia media = mediaRepository.findByRelatedMediaKey(mediaKey)
                .orElseThrow(() -> new RuntimeException("Media not found"));

        if (!media.getCompany().getId().equals(company.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            fileUploadService.deleteFile(media.getLocation());
        } catch (IOException e) {
            System.err.println("Failed to delete file from storage: " + e.getMessage());
        }

        mediaRepository.delete(media);

        return ResponseEntity.ok(Map.of("message", "Media deleted successfully"));
    }

    private Map<String, Object> mapToResponse(ResearchRelatedMedia media) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", media.getId());
        response.put("mediaKey", media.getRelatedMediaKey());
        response.put("location", media.getLocation());
        response.put("mimeType", media.getMimeType());
        response.put("mediaType", media.getMediaType());
        response.put("poster", media.getPoster());
        response.put("fileName", media.getFileName());
        response.put("fileSize", media.getFileSize());
        if (media.getResearchArticle() != null) {
            response.put("articleKey", media.getResearchArticle().getArticleKey());
        }
        if (media.getResearchJournal() != null) {
            response.put("journalKey", media.getResearchJournal().getJournalKey());
        }
        if (media.getResearchJournalVolume() != null) {
            response.put("volumeKey", media.getResearchJournalVolume().getVolumeKey());
        }
        response.put("createdAt", media.getCreatedAt());
        return response;
    }

    private String determineMediaType(String mimeType) {
        if (mimeType == null) {
            return "unknown";
        }
        if (mimeType.startsWith("image/")) {
            return "image";
        } else if (mimeType.startsWith("video/")) {
            return "video";
        } else if (mimeType.startsWith("audio/")) {
            return "audio";
        } else if (mimeType.equals("application/pdf")) {
            return "document";
        } else {
            return "file";
        }
    }
}

