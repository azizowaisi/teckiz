package com.teckiz.controller.admin.website;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebNewsType;
import com.teckiz.repository.WebNewsTypeRepository;
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
@RequestMapping("/website/admin/news-types")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class WebNewsTypeController {

    private final ModuleAccessManager moduleAccessManager;
    private final WebNewsTypeRepository newsTypeRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listNewsTypes() {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        List<WebNewsType> newsTypes = newsTypeRepository.findByCompanyModuleMapper(companyModuleMapper);

        List<Map<String, Object>> typeResponses = newsTypes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("newsTypes", typeResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{typeKey}")
    public ResponseEntity<Map<String, Object>> getNewsType(@PathVariable String typeKey) {
        moduleAccessManager.authenticateModule();

        return newsTypeRepository.findByTypeKey(typeKey)
                .map(newsType -> ResponseEntity.ok(mapToResponse(newsType)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createNewsType(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String name = (String) request.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }

        WebNewsType newsType = WebNewsType.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .name(name)
                .description((String) request.get("description"))
                .build();

        newsType = newsTypeRepository.save(newsType);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "News type created successfully", "typeKey", newsType.getTypeKey()));
    }

    @PutMapping("/{typeKey}")
    public ResponseEntity<?> updateNewsType(
            @PathVariable String typeKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebNewsType newsType = newsTypeRepository.findByTypeKey(typeKey)
                .orElseThrow(() -> new RuntimeException("News type not found"));

        if (newsType.getCompanyModuleMapper() == null ||
                !newsType.getCompanyModuleMapper().getId().equals(companyModuleMapper.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("name") != null) {
            newsType.setName((String) request.get("name"));
        }
        if (request.get("description") != null) {
            newsType.setDescription((String) request.get("description"));
        }

        newsType = newsTypeRepository.save(newsType);

        return ResponseEntity.ok(Map.of("message", "News type updated successfully"));
    }

    @DeleteMapping("/{typeKey}")
    public ResponseEntity<?> deleteNewsType(@PathVariable String typeKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebNewsType newsType = newsTypeRepository.findByTypeKey(typeKey)
                .orElseThrow(() -> new RuntimeException("News type not found"));

        if (newsType.getCompanyModuleMapper() == null ||
                !newsType.getCompanyModuleMapper().getId().equals(companyModuleMapper.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        newsTypeRepository.delete(newsType);

        return ResponseEntity.ok(Map.of("message", "News type deleted successfully"));
    }

    private Map<String, Object> mapToResponse(WebNewsType newsType) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", newsType.getId());
        response.put("typeKey", newsType.getTypeKey());
        response.put("name", newsType.getName());
        response.put("description", newsType.getDescription());
        return response;
    }
}

