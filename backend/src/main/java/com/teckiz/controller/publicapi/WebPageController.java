package com.teckiz.controller.publicapi;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebPage;
import com.teckiz.repository.WebPageRepository;
import com.teckiz.service.WebsiteManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Public - WebPage", description = "Public API endpoints for WebPage")
@RequestMapping("/public/pages")
@RequiredArgsConstructor
@org.springframework.stereotype.Component("publicWebPageController")
public class WebPageController {

    private final WebsiteManager websiteManager;
    private final WebPageRepository webPageRepository;

    @GetMapping("/{slug}")
    public ResponseEntity<Map<String, Object>> getPageBySlug(@PathVariable String slug) {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        WebPage webPage = webPageRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Page not found"));

        // Verify page belongs to company
        if (!webPage.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        // Get non-archived contacts
        List<Map<String, Object>> contacts = webPage.getContacts() != null ?
                webPage.getContacts().stream()
                        .filter(contact -> !Boolean.TRUE.equals(contact.getArchived()))
                        .map(contact -> {
                            Map<String, Object> contactMap = new HashMap<>();
                            contactMap.put("id", contact.getId());
                            contactMap.put("name", contact.getName());
                            contactMap.put("role", contact.getRole());
                            contactMap.put("email", contact.getEmail());
                            contactMap.put("phone", contact.getPhone());
                            contactMap.put("description", contact.getDescription());
                            return contactMap;
                        })
                        .collect(Collectors.toList()) : List.of();

        Map<String, Object> response = new HashMap<>();
        response.put("id", webPage.getId());
        response.put("pageKey", webPage.getPageKey());
        response.put("title", webPage.getTitle());
        response.put("slug", webPage.getSlug());
        response.put("shortDescription", webPage.getShortDescription());
        response.put("description", webPage.getDescription());
        response.put("thumbnail", webPage.getThumbnail());
        response.put("contacts", contacts);
        response.put("createdAt", webPage.getCreatedAt());
        response.put("updatedAt", webPage.getUpdatedAt());

        return ResponseEntity.ok(response);
    }
}

