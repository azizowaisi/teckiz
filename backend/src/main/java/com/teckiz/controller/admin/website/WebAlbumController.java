package com.teckiz.controller.admin.website;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebAlbum;
import com.teckiz.repository.WebAlbumRepository;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/website/admin/albums")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class WebAlbumController {

    private final ModuleAccessManager moduleAccessManager;
    private final WebAlbumRepository webAlbumRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listAlbums(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchKey,
            @RequestParam(required = false) String filter) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WebAlbum> albums;

        if ("published".equals(filter)) {
            albums = webAlbumRepository.findByCompanyAndPublishedTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(),
                    pageable
            );
        } else {
            albums = webAlbumRepository.findByCompany(companyModuleMapper.getCompany(), pageable);
        }

        List<Map<String, Object>> albumResponses = albums.getContent().stream()
                .map(album -> {
                    Map<String, Object> albumMap = new HashMap<>();
                    albumMap.put("id", album.getId());
                    albumMap.put("albumKey", album.getAlbumKey());
                    albumMap.put("title", album.getTitle());
                    albumMap.put("slug", album.getSlug());
                    albumMap.put("description", album.getDescription());
                    albumMap.put("published", album.getPublished());
                    albumMap.put("archived", album.getArchived());
                    albumMap.put("carousal", album.getCarousal());
                    albumMap.put("publishedAt", album.getPublishedAt());
                    albumMap.put("createdAt", album.getCreatedAt());
                    return albumMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("albums", albumResponses);
        response.put("totalPages", albums.getTotalPages());
        response.put("totalElements", albums.getTotalElements());
        response.put("currentPage", page);
        response.put("leftTab", "album");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createAlbum(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String title = (String) request.get("title");
        if (title == null || title.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Title is required"));
        }

        WebAlbum album = WebAlbum.builder()
                .company(companyModuleMapper.getCompany())
                .title(title)
                .slug(generateSlug(title))
                .description((String) request.get("description"))
                .published(request.get("published") != null ? (Boolean) request.get("published") : false)
                .publishedAt(request.get("published") != null && (Boolean) request.get("published") ? 
                        LocalDateTime.now() : null)
                .carousal(request.get("carousal") != null ? (Boolean) request.get("carousal") : false)
                .build();

        album = webAlbumRepository.save(album);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Album created successfully", "albumKey", album.getAlbumKey()));
    }

    @PutMapping("/{albumKey}")
    public ResponseEntity<?> updateAlbum(
            @PathVariable String albumKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebAlbum album = webAlbumRepository.findByAlbumKey(albumKey)
                .orElseThrow(() -> new RuntimeException("Album not found"));

        if (!album.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("title") != null) {
            album.setTitle((String) request.get("title"));
            album.setSlug(generateSlug((String) request.get("title")));
        }
        if (request.get("description") != null) {
            album.setDescription((String) request.get("description"));
        }
        if (request.get("published") != null) {
            album.setPublished((Boolean) request.get("published"));
            if ((Boolean) request.get("published") && album.getPublishedAt() == null) {
                album.setPublishedAt(LocalDateTime.now());
            }
        }
        if (request.get("carousal") != null) {
            album.setCarousal((Boolean) request.get("carousal"));
        }

        album = webAlbumRepository.save(album);

        return ResponseEntity.ok(Map.of("message", "Album updated successfully"));
    }

    @DeleteMapping("/{albumKey}")
    public ResponseEntity<?> deleteAlbum(@PathVariable String albumKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebAlbum album = webAlbumRepository.findByAlbumKey(albumKey)
                .orElseThrow(() -> new RuntimeException("Album not found"));

        if (!album.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        album.setArchived(true);
        webAlbumRepository.save(album);

        return ResponseEntity.ok(Map.of("message", "Album archived successfully"));
    }

    private String generateSlug(String title) {
        if (title == null) {
            return null;
        }
        return title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}

