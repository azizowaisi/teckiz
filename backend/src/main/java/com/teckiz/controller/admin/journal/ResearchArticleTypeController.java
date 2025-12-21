package com.teckiz.controller.admin.journal;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.Company;
import com.teckiz.entity.ResearchArticleType;
import com.teckiz.repository.ResearchArticleTypeRepository;
import com.teckiz.service.ModuleAccessManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/journal/admin/article-types")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class ResearchArticleTypeController {

    private final ModuleAccessManager moduleAccessManager;
    private final ResearchArticleTypeRepository articleTypeRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listArticleTypes() {
        Company company = moduleAccessManager.authenticateModule().getCompany();

        List<ResearchArticleType> articleTypes = articleTypeRepository.findByCompany(company);

        List<Map<String, Object>> typeResponses = articleTypes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("articleTypes", typeResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{typeKey}")
    public ResponseEntity<Map<String, Object>> getArticleType(@PathVariable String typeKey) {
        moduleAccessManager.authenticateModule();

        return articleTypeRepository.findByTypeKey(typeKey)
                .map(articleType -> ResponseEntity.ok(mapToResponse(articleType)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createArticleType(@RequestBody Map<String, Object> request) {
        Company company = moduleAccessManager.authenticateModule().getCompany();

        String name = (String) request.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }

        ResearchArticleType articleType = ResearchArticleType.builder()
                .company(company)
                .name(name)
                .build();

        articleType = articleTypeRepository.save(articleType);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Article type created successfully", "typeKey", articleType.getTypeKey()));
    }

    @PutMapping("/{typeKey}")
    public ResponseEntity<?> updateArticleType(
            @PathVariable String typeKey,
            @RequestBody Map<String, Object> request) {

        Company company = moduleAccessManager.authenticateModule().getCompany();

        ResearchArticleType articleType = articleTypeRepository.findByTypeKey(typeKey)
                .orElseThrow(() -> new RuntimeException("Article type not found"));

        if (!articleType.getCompany().getId().equals(company.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("name") != null) {
            articleType.setName((String) request.get("name"));
        }

        articleType = articleTypeRepository.save(articleType);

        return ResponseEntity.ok(Map.of("message", "Article type updated successfully"));
    }

    @DeleteMapping("/{typeKey}")
    public ResponseEntity<?> deleteArticleType(@PathVariable String typeKey) {
        Company company = moduleAccessManager.authenticateModule().getCompany();

        ResearchArticleType articleType = articleTypeRepository.findByTypeKey(typeKey)
                .orElseThrow(() -> new RuntimeException("Article type not found"));

        if (!articleType.getCompany().getId().equals(company.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        articleTypeRepository.delete(articleType);

        return ResponseEntity.ok(Map.of("message", "Article type deleted successfully"));
    }

    private Map<String, Object> mapToResponse(ResearchArticleType articleType) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", articleType.getId());
        response.put("typeKey", articleType.getTypeKey());
        response.put("name", articleType.getName());
        return response;
    }
}

