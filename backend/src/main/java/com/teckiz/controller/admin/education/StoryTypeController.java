package com.teckiz.controller.admin.education;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.StoryType;
import com.teckiz.repository.StoryTypeRepository;
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
@RequestMapping("/education/admin/story-types")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class StoryTypeController {

    private final ModuleAccessManager moduleAccessManager;
    private final StoryTypeRepository storyTypeRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listStoryTypes() {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        List<StoryType> storyTypes = storyTypeRepository.findByCompanyModuleMapper(companyModuleMapper);

        List<Map<String, Object>> typeResponses = storyTypes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("storyTypes", typeResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{typeKey}")
    public ResponseEntity<Map<String, Object>> getStoryType(@PathVariable String typeKey) {
        moduleAccessManager.authenticateModule();

        return storyTypeRepository.findByTypeKey(typeKey)
                .map(storyType -> ResponseEntity.ok(mapToResponse(storyType)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createStoryType(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String name = (String) request.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }

        StoryType storyType = StoryType.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .name(name)
                .description((String) request.get("description"))
                .build();

        storyType = storyTypeRepository.save(storyType);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Story type created successfully", "typeKey", storyType.getTypeKey()));
    }

    @PutMapping("/{typeKey}")
    public ResponseEntity<?> updateStoryType(
            @PathVariable String typeKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        StoryType storyType = storyTypeRepository.findByTypeKey(typeKey)
                .orElseThrow(() -> new RuntimeException("Story type not found"));

        if (storyType.getCompanyModuleMapper() == null ||
                !storyType.getCompanyModuleMapper().getId().equals(companyModuleMapper.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("name") != null) {
            storyType.setName((String) request.get("name"));
        }
        if (request.get("description") != null) {
            storyType.setDescription((String) request.get("description"));
        }

        storyType = storyTypeRepository.save(storyType);

        return ResponseEntity.ok(Map.of("message", "Story type updated successfully"));
    }

    @DeleteMapping("/{typeKey}")
    public ResponseEntity<?> deleteStoryType(@PathVariable String typeKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        StoryType storyType = storyTypeRepository.findByTypeKey(typeKey)
                .orElseThrow(() -> new RuntimeException("Story type not found"));

        if (storyType.getCompanyModuleMapper() == null ||
                !storyType.getCompanyModuleMapper().getId().equals(companyModuleMapper.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        storyTypeRepository.delete(storyType);

        return ResponseEntity.ok(Map.of("message", "Story type deleted successfully"));
    }

    private Map<String, Object> mapToResponse(StoryType storyType) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", storyType.getId());
        response.put("typeKey", storyType.getTypeKey());
        response.put("name", storyType.getName());
        response.put("description", storyType.getDescription());
        return response;
    }
}

