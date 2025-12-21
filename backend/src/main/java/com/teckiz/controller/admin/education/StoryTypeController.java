package com.teckiz.controller.admin.education;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.dto.StoryTypeRequest;
import com.teckiz.dto.StoryTypeResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.StoryType;
import com.teckiz.repository.StoryTypeRepository;
import com.teckiz.service.ModuleAccessManager;
import jakarta.validation.Valid;
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

        List<StoryTypeResponse> typeResponses = storyTypes.stream()
                .map(this::mapToResponse)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("storyTypes", typeResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{typeKey}")
    public ResponseEntity<StoryTypeResponse> getStoryType(@PathVariable String typeKey) {
        moduleAccessManager.authenticateModule();

        return storyTypeRepository.findByTypeKey(typeKey)
                .map(storyType -> ResponseEntity.ok(mapToResponse(storyType)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createStoryType(@Valid @RequestBody StoryTypeRequest request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        StoryType storyType = StoryType.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        storyType = storyTypeRepository.save(storyType);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Story type created successfully", "typeKey", storyType.getTypeKey()));
    }

    @PutMapping("/{typeKey}")
    public ResponseEntity<?> updateStoryType(
            @PathVariable String typeKey,
            @Valid @RequestBody StoryTypeRequest request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        StoryType storyType = storyTypeRepository.findByTypeKey(typeKey)
                .orElseThrow(() -> new RuntimeException("Story type not found"));

        if (storyType.getCompanyModuleMapper() == null ||
                !storyType.getCompanyModuleMapper().getId().equals(companyModuleMapper.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getName() != null) {
            storyType.setName(request.getName());
        }
        if (request.getDescription() != null) {
            storyType.setDescription(request.getDescription());
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

    private StoryTypeResponse mapToResponse(StoryType storyType) {
        return StoryTypeResponse.builder()
                .id(storyType.getId())
                .typeKey(storyType.getTypeKey())
                .name(storyType.getName())
                .description(storyType.getDescription())
                .companyId(storyType.getCompany().getId())
                .companyName(storyType.getCompany().getName())
                .build();
    }
}

