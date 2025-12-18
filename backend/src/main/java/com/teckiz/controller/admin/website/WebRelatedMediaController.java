package com.teckiz.controller.admin.website;

import com.teckiz.entity.Company;
import com.teckiz.entity.WebRelatedMedia;
import com.teckiz.repository.WebRelatedMediaRepository;
import com.teckiz.service.FileUploadService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/website/admin/media")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class WebRelatedMediaController {

    private final ModuleAccessManager moduleAccessManager;
    private final WebRelatedMediaRepository mediaRepository;
    private final FileUploadService fileUploadService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listMedia(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String mediaType,
            @RequestParam(required = false) Boolean poster) {

        Company company = moduleAccessManager.authenticateModule().getCompany();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WebRelatedMedia> mediaPage;

        if (poster != null && poster) {
            mediaPage = mediaRepository.findByCompanyAndPosterTrue(company, pageable);
        } else {
            mediaPage = mediaRepository.findByCompany(company, pageable);
        }

        // Filter by media type if provided
        List<WebRelatedMedia> filteredMedia = mediaPage.getContent();
        if (mediaType != null && !mediaType.isEmpty()) {
            filteredMedia = filteredMedia.stream()
                    .filter(media -> mediaType.equals(media.getMediaType()))
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> mediaResponses = filteredMedia.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("media", mediaResponses);
        response.put("totalPages", mediaPage.getTotalPages());
        response.put("totalElements", mediaPage.getTotalElements());
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

        WebRelatedMedia media = WebRelatedMedia.builder()
                .company(company)
                .location(location)
                .mimeType((String) request.get("mimeType"))
                .mediaType((String) request.get("mediaType"))
                .poster(request.get("poster") != null ?
                        (Boolean) request.get("poster") : false)
                .build();

        media = mediaRepository.save(media);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Media created successfully", "mediaKey", media.getRelatedMediaKey()));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(@RequestParam("file") MultipartFile file) {
        Company company = moduleAccessManager.authenticateModule().getCompany();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty"));
        }

        // Upload file using FileUploadService
        String location;
        try {
            location = fileUploadService.uploadFile(file, "media");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
        
        String fileName = file.getOriginalFilename();
        String mimeType = file.getContentType();

        WebRelatedMedia media = WebRelatedMedia.builder()
                .company(company)
                .location(location)
                .mimeType(mimeType)
                .mediaType(determineMediaType(mimeType))
                .poster(false)
                .build();

        media = mediaRepository.save(media);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "File uploaded successfully",
                        "mediaKey", media.getRelatedMediaKey(),
                        "location", location
                ));
    }

    @PutMapping("/{mediaKey}")
    public ResponseEntity<?> updateMedia(
            @PathVariable String mediaKey,
            @RequestBody Map<String, Object> request) {

        Company company = moduleAccessManager.authenticateModule().getCompany();

        WebRelatedMedia media = mediaRepository.findByRelatedMediaKey(mediaKey)
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

        media = mediaRepository.save(media);

        return ResponseEntity.ok(Map.of("message", "Media updated successfully"));
    }

    @DeleteMapping("/{mediaKey}")
    public ResponseEntity<?> deleteMedia(@PathVariable String mediaKey) {
        Company company = moduleAccessManager.authenticateModule().getCompany();

        WebRelatedMedia media = mediaRepository.findByRelatedMediaKey(mediaKey)
                .orElseThrow(() -> new RuntimeException("Media not found"));

        if (!media.getCompany().getId().equals(company.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // TODO: Delete actual file from storage before deleting record
        mediaRepository.delete(media);

        return ResponseEntity.ok(Map.of("message", "Media deleted successfully"));
    }

    private Map<String, Object> mapToResponse(WebRelatedMedia media) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", media.getId());
        response.put("mediaKey", media.getRelatedMediaKey());
        response.put("location", media.getLocation());
        response.put("mimeType", media.getMimeType());
        response.put("mediaType", media.getMediaType());
        response.put("poster", media.getPoster());
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

