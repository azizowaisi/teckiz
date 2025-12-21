package com.teckiz.controller.admin.website;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.dto.WebNewsRequest;
import com.teckiz.dto.WebNewsResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebContacts;
import com.teckiz.entity.WebNews;
import com.teckiz.repository.WebContactsRepository;
import com.teckiz.repository.WebNewsRepository;
import com.teckiz.repository.WebNewsTypeRepository;
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
@RequestMapping("/website/admin/news")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class WebNewsController {

    private final ModuleAccessManager moduleAccessManager;
    private final WebNewsRepository webNewsRepository;
    private final WebNewsTypeRepository webNewsTypeRepository;
    private final WebContactsRepository webContactsRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchKey,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Long newsTypeId) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<WebNews> newsPage;

        if ("published".equals(filter)) {
            newsPage = webNewsRepository.findByCompanyAndPublishedTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(),
                    pageable
            );
        } else if ("archived".equals(filter)) {
            // Need to add archived filter method
            newsPage = webNewsRepository.findByCompany(companyModuleMapper.getCompany(), pageable);
        } else {
            newsPage = webNewsRepository.findByCompany(companyModuleMapper.getCompany(), pageable);
        }

        List<WebNewsResponse> newsResponses = newsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("news", newsResponses);
        response.put("totalPages", newsPage.getTotalPages());
        response.put("totalElements", newsPage.getTotalElements());
        response.put("currentPage", page);
        response.put("leftTab", "web-news");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{newsKey}")
    public ResponseEntity<WebNewsResponse> getNews(@PathVariable String newsKey) {
        moduleAccessManager.authenticateModule();
        
        return webNewsRepository.findByNewsKey(newsKey)
                .map(news -> ResponseEntity.ok(mapToResponse(news)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WebNewsResponse> createNews(@RequestBody WebNewsRequest request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        WebNews webNews = WebNews.builder()
                .company(companyModuleMapper.getCompany())
                .title(request.getTitle())
                .slug(generateSlug(request.getTitle()))
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .published(request.getPublished() != null ? request.getPublished() : false)
                .publishedAt(request.getPublishedAt() != null ? request.getPublishedAt() : 
                        (request.getPublished() != null && request.getPublished() ? LocalDateTime.now() : null))
                .carousel(request.getCarousel() != null ? request.getCarousel() : false)
                .embedCode(request.getEmbedCode())
                .build();

        // Set news type if provided
        if (request.getWebNewsTypeId() != null) {
            webNewsTypeRepository.findById(request.getWebNewsTypeId())
                    .ifPresent(webNews::setWebNewsType);
        }

        // Add contacts if provided
        if (request.getContactKeys() != null && !request.getContactKeys().isEmpty()) {
            List<WebContacts> contacts = request.getContactKeys().stream()
                    .map(key -> webContactsRepository.findByContactKey(key))
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toList());
            webNews.setContacts(contacts);
        }

        webNews = webNewsRepository.save(webNews);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapToResponse(webNews));
    }

    @PutMapping("/{newsKey}")
    public ResponseEntity<WebNewsResponse> updateNews(
            @PathVariable String newsKey,
            @RequestBody WebNewsRequest request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebNews webNews = webNewsRepository.findByNewsKey(newsKey)
                .orElseThrow(() -> new RuntimeException("News not found"));

        // Verify news belongs to company
        if (!webNews.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        webNews.setTitle(request.getTitle());
        webNews.setSlug(generateSlug(request.getTitle()));
        webNews.setShortDescription(request.getShortDescription());
        webNews.setDescription(request.getDescription());
        if (request.getPublished() != null) {
            webNews.setPublished(request.getPublished());
            if (request.getPublished() && webNews.getPublishedAt() == null) {
                webNews.setPublishedAt(LocalDateTime.now());
            }
        }
        if (request.getPublishedAt() != null) {
            webNews.setPublishedAt(request.getPublishedAt());
        }
        if (request.getCarousel() != null) {
            webNews.setCarousel(request.getCarousel());
        }
        webNews.setEmbedCode(request.getEmbedCode());

        // Update news type
        if (request.getWebNewsTypeId() != null) {
            webNewsTypeRepository.findById(request.getWebNewsTypeId())
                    .ifPresent(webNews::setWebNewsType);
        }

        // Update contacts
        if (request.getContactKeys() != null) {
            List<WebContacts> contacts = request.getContactKeys().stream()
                    .map(key -> webContactsRepository.findByContactKey(key))
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toList());
            webNews.setContacts(contacts);
        }

        webNews = webNewsRepository.save(webNews);

        return ResponseEntity.ok(mapToResponse(webNews));
    }

    @DeleteMapping("/{newsKey}")
    public ResponseEntity<?> deleteNews(@PathVariable String newsKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebNews webNews = webNewsRepository.findByNewsKey(newsKey)
                .orElseThrow(() -> new RuntimeException("News not found"));

        // Verify news belongs to company
        if (!webNews.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        webNews.setArchived(true);
        webNewsRepository.save(webNews);

        return ResponseEntity.ok(Map.of("message", "News archived successfully"));
    }

    private WebNewsResponse mapToResponse(WebNews news) {
        return WebNewsResponse.builder()
                .id(news.getId())
                .newsKey(news.getNewsKey())
                .title(news.getTitle())
                .slug(news.getSlug())
                .shortDescription(news.getShortDescription())
                .description(news.getDescription())
                .published(news.getPublished())
                .publishedAt(news.getPublishedAt())
                .archived(news.getArchived())
                .carousel(news.getCarousel())
                .embedCode(news.getEmbedCode())
                .posterId(news.getPoster() != null ? news.getPoster().getId() : null)
                .companyId(news.getCompany().getId())
                .companyName(news.getCompany().getName())
                .webNewsTypeId(news.getWebNewsType() != null ? news.getWebNewsType().getId() : null)
                .webNewsTypeName(news.getWebNewsType() != null ? news.getWebNewsType().getName() : null)
                .contacts(news.getContacts() != null ? news.getContacts().stream()
                        .map(contact -> WebNewsResponse.ContactInfo.builder()
                                .id(contact.getId())
                                .name(contact.getName())
                                .email(contact.getEmail())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .createdAt(news.getCreatedAt())
                .updatedAt(news.getUpdatedAt())
                .build();
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

