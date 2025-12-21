package com.teckiz.controller.admin.website;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.dto.WebPageRequest;
import com.teckiz.dto.WebPageResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebContacts;
import com.teckiz.entity.WebPage;
import com.teckiz.repository.WebContactsRepository;
import com.teckiz.repository.WebPageRepository;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/website/admin/pages")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class WebPageController {

    private final ModuleAccessManager moduleAccessManager;
    private final WebPageRepository webPageRepository;
    private final WebContactsRepository webContactsRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listPages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String searchKey,
            @RequestParam(required = false) String filter) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WebPage> pages;

        if (searchKey != null && !searchKey.isEmpty()) {
            pages = webPageRepository.findByCompanyAndSlugContaining(
                    companyModuleMapper.getCompany(),
                    searchKey,
                    pageable
            );
        } else {
            pages = webPageRepository.findByCompany(
                    companyModuleMapper.getCompany(),
                    pageable
            );
        }

        List<WebPageResponse> pageResponses = pages.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("pages", pageResponses);
        response.put("totalPages", pages.getTotalPages());
        response.put("totalElements", pages.getTotalElements());
        response.put("currentPage", page);
        response.put("leftTab", "web-page");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pageKey}")
    public ResponseEntity<WebPageResponse> getPage(@PathVariable String pageKey) {
        moduleAccessManager.authenticateModule();
        
        return webPageRepository.findByPageKey(pageKey)
                .map(page -> ResponseEntity.ok(mapToResponse(page)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WebPageResponse> createPage(@RequestBody WebPageRequest request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        WebPage webPage = WebPage.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .title(request.getTitle())
                .slug(generateSlug(request.getTitle()))
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .thumbnail(request.getThumbnail())
                .build();

        // Add contacts if provided
        if (request.getContactKeys() != null && !request.getContactKeys().isEmpty()) {
            List<WebContacts> contacts = request.getContactKeys().stream()
                    .map(key -> webContactsRepository.findByContactKey(key))
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toList());
            webPage.setContacts(contacts);
        }

        webPage = webPageRepository.save(webPage);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapToResponse(webPage));
    }

    @PutMapping("/{pageKey}")
    public ResponseEntity<WebPageResponse> updatePage(
            @PathVariable String pageKey,
            @RequestBody WebPageRequest request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebPage webPage = webPageRepository.findByPageKey(pageKey)
                .orElseThrow(() -> new RuntimeException("Page not found"));

        // Verify page belongs to company
        if (!webPage.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        webPage.setTitle(request.getTitle());
        webPage.setSlug(generateSlug(request.getTitle()));
        webPage.setShortDescription(request.getShortDescription());
        webPage.setDescription(request.getDescription());
        webPage.setThumbnail(request.getThumbnail());

        // Update contacts
        if (request.getContactKeys() != null) {
            List<WebContacts> contacts = request.getContactKeys().stream()
                    .map(key -> webContactsRepository.findByContactKey(key))
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toList());
            webPage.setContacts(contacts);
        }

        webPage = webPageRepository.save(webPage);

        return ResponseEntity.ok(mapToResponse(webPage));
    }

    @DeleteMapping("/{pageKey}")
    public ResponseEntity<?> deletePage(@PathVariable String pageKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebPage webPage = webPageRepository.findByPageKey(pageKey)
                .orElseThrow(() -> new RuntimeException("Page not found"));

        // Verify page belongs to company
        if (!webPage.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        webPageRepository.delete(webPage);

        return ResponseEntity.ok(Map.of("message", "Page deleted successfully"));
    }

    private WebPageResponse mapToResponse(WebPage page) {
        return WebPageResponse.builder()
                .id(page.getId())
                .pageKey(page.getPageKey())
                .title(page.getTitle())
                .slug(page.getSlug())
                .shortDescription(page.getShortDescription())
                .description(page.getDescription())
                .thumbnail(page.getThumbnail())
                .posterId(page.getPoster() != null ? page.getPoster().getId() : null)
                .companyId(page.getCompany().getId())
                .companyName(page.getCompany().getName())
                .companyModuleMapperId(page.getCompanyModuleMapper() != null ? 
                        page.getCompanyModuleMapper().getId() : null)
                .contacts(page.getContacts() != null ? page.getContacts().stream()
                        .map(contact -> WebPageResponse.ContactInfo.builder()
                                .id(contact.getId())
                                .name(contact.getName())
                                .email(contact.getEmail())
                                .role(contact.getRole())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .createdAt(page.getCreatedAt())
                .updatedAt(page.getUpdatedAt())
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

