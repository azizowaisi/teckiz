package com.teckiz.controller.admin.education;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.dto.StoryRequest;
import com.teckiz.dto.StoryResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Story;
import com.teckiz.entity.StoryType;
import com.teckiz.repository.StoryRepository;
import com.teckiz.repository.StoryTypeRepository;
import com.teckiz.service.ModuleAccessManager;
import jakarta.validation.Valid;
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
@RequestMapping("/education/admin/stories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
@org.springframework.stereotype.Component("adminStoryController")
public class StoryController {

    private final ModuleAccessManager moduleAccessManager;
    private final StoryRepository storyRepository;
    private final StoryTypeRepository storyTypeRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) String storyTypeKey) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Story> stories;

        if (published != null && published) {
            stories = storyRepository.findByCompanyAndPublishedTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(), pageable);
        } else {
            stories = storyRepository.findByCompany(companyModuleMapper.getCompany(), pageable);
        }

        // Filter by story type if provided
        if (storyTypeKey != null && !storyTypeKey.isEmpty()) {
            StoryType storyType = storyTypeRepository.findByTypeKey(storyTypeKey).orElse(null);
            if (storyType != null) {
                List<Story> filtered = stories.getContent().stream()
                        .filter(story -> story.getStoryType() != null &&
                                story.getStoryType().getId().equals(storyType.getId()))
                        .collect(Collectors.toList());
                
                int start = page * size;
                int end = Math.min(start + size, filtered.size());
                List<Story> paginated = filtered.subList(start, end);
                
                Map<String, Object> response = new HashMap<>();
                response.put("stories", paginated.stream()
                        .map(this::mapToResponse)
                        .toList());
                response.put("totalPages", (int) Math.ceil((double) filtered.size() / size));
                response.put("totalElements", filtered.size());
                response.put("currentPage", page);
                return ResponseEntity.ok(response);
            }
        }

        List<StoryResponse> storyResponses = stories.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("stories", storyResponses);
        response.put("totalPages", stories.getTotalPages());
        response.put("totalElements", stories.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{storyKey}")
    public ResponseEntity<StoryResponse> getStory(@PathVariable String storyKey) {
        moduleAccessManager.authenticateModule();

        return storyRepository.findByStoryKey(storyKey)
                .map(story -> ResponseEntity.ok(mapToResponse(story)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createStory(@Valid @RequestBody StoryRequest request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Story story = Story.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnail(request.getThumbnail())
                .published(request.getPublished() != null ? request.getPublished() : false)
                .archived(false)
                .build();

        // Set story type if provided
        if (request.getStoryTypeKey() != null && !request.getStoryTypeKey().isEmpty()) {
            storyTypeRepository.findByTypeKey(request.getStoryTypeKey())
                    .ifPresent(story::setStoryType);
        }

        story = storyRepository.save(story);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Story created successfully", "storyKey", story.getStoryKey()));
    }

    @PutMapping("/{storyKey}")
    public ResponseEntity<?> updateStory(
            @PathVariable String storyKey,
            @Valid @RequestBody StoryRequest request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Story story = storyRepository.findByStoryKey(storyKey)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        if (!story.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getTitle() != null) {
            story.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            story.setDescription(request.getDescription());
        }
        if (request.getThumbnail() != null) {
            story.setThumbnail(request.getThumbnail());
        }
        if (request.getPublished() != null) {
            story.setPublished(request.getPublished());
        }
        if (request.getStoryTypeKey() != null && !request.getStoryTypeKey().isEmpty()) {
            storyTypeRepository.findByTypeKey(request.getStoryTypeKey())
                    .ifPresent(story::setStoryType);
        }

        story = storyRepository.save(story);

        return ResponseEntity.ok(Map.of("message", "Story updated successfully"));
    }

    @DeleteMapping("/{storyKey}")
    public ResponseEntity<?> deleteStory(@PathVariable String storyKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Story story = storyRepository.findByStoryKey(storyKey)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        if (!story.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        story.setArchived(true);
        storyRepository.save(story);

        return ResponseEntity.ok(Map.of("message", "Story deleted successfully"));
    }

    private StoryResponse mapToResponse(Story story) {
        StoryResponse.StoryResponseBuilder builder = StoryResponse.builder()
                .id(story.getId())
                .storyKey(story.getStoryKey())
                .title(story.getTitle())
                .description(story.getDescription())
                .thumbnail(story.getThumbnail())
                .published(story.getPublished())
                .archived(story.getArchived())
                .companyId(story.getCompany().getId())
                .companyName(story.getCompany().getName())
                .createdAt(story.getCreatedAt())
                .updatedAt(story.getUpdatedAt());

        if (story.getStoryType() != null) {
            builder.storyTypeId(story.getStoryType().getId())
                   .storyTypeName(story.getStoryType().getName());
        }

        return builder.build();
    }
}

