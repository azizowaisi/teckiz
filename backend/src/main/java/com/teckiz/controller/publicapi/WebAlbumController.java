package com.teckiz.controller.publicapi;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebAlbum;
import com.teckiz.repository.WebAlbumRepository;
import com.teckiz.service.WebsiteManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Public - WebAlbum", description = "Public API endpoints for WebAlbum")
@RequestMapping("/public/albums")
@RequiredArgsConstructor
@org.springframework.stereotype.Component("publicWebAlbumController")
public class WebAlbumController {

    private final WebsiteManager websiteManager;
    private final WebAlbumRepository webAlbumRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listPublishedAlbums(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WebAlbum> albums = webAlbumRepository.findByCompanyAndPublishedTrueAndArchivedFalse(
                companyModuleMapper.getCompany(),
                pageable
        );

        List<Map<String, Object>> albumResponses = albums.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("albums", albumResponses);
        response.put("totalPages", albums.getTotalPages());
        response.put("totalElements", albums.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Map<String, Object>> getAlbumBySlug(@PathVariable String slug) {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        WebAlbum album = webAlbumRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Album not found"));

        // Verify album belongs to company and is published
        if (!album.getCompany().getId().equals(companyModuleMapper.getCompany().getId()) ||
                !Boolean.TRUE.equals(album.getPublished()) ||
                Boolean.TRUE.equals(album.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(album));
    }

    @GetMapping("/carousel")
    public ResponseEntity<List<Map<String, Object>>> getCarouselAlbums() {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        List<WebAlbum> carouselAlbums = webAlbumRepository.findByCompanyAndCarousalTrueAndPublishedTrue(
                companyModuleMapper.getCompany()
        );

        List<Map<String, Object>> albumResponses = carouselAlbums.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(albumResponses);
    }

    private Map<String, Object> mapToResponse(WebAlbum album) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", album.getId());
        response.put("albumKey", album.getAlbumKey());
        response.put("title", album.getTitle());
        response.put("slug", album.getSlug());
        response.put("description", album.getDescription());
        response.put("carousal", album.getCarousal());
        response.put("publishedAt", album.getPublishedAt());
        response.put("createdAt", album.getCreatedAt());
        return response;
    }
}

