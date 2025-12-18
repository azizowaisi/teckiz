package com.teckiz.controller.admin.education;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Story;
import com.teckiz.entity.StoryType;
import com.teckiz.repository.StoryRepository;
import com.teckiz.repository.StoryTypeRepository;
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
@RequestMapping("/education/admin/stories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
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
                        .collect(Collectors.toList()));
                response.put("totalPages", (int) Math.ceil((double) filtered.size() / size));
                response.put("totalElements", filtered.size());
                response.put("currentPage", page);
                return ResponseEntity.ok(response);
            }
        }

        List<Map<String, Object>> storyResponses = stories.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("stories", storyResponses);
        response.put("totalPages", stories.getTotalPages());
        response.put("totalElements", stories.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{storyKey}")
    public ResponseEntity<Map<String, Object>> getStory(@PathVariable String storyKey) {
        moduleAccessManager.authenticateModule();

        return storyRepository.findByStoryKey(storyKey)
                .map(story -> ResponseEntity.ok(mapToResponse(story)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createStory(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String title = (String) request.get("title");
        if (title == null || title.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Title is required"));
        }

        Story story = Story.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .title(title)
                .description((String) request.get("description"))
                .thumbnail((String) request.get("thumbnail"))
                .published(request.get("published") != null ?
                        (Boolean) request.get("published") : false)
                .archived(false)
                .build();

        // Set story type if provided
        if (request.get("storyTypeKey") != null) {
            storyTypeRepository.findByTypeKey((String) request.get("storyTypeKey"))
                    .ifPresent(story::setStoryType);
        }

        story = storyRepository.save(story);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Story created successfully", "storyKey", story.getStoryKey()));
    }

    @PutMapping("/{storyKey}")
    public ResponseEntity<?> updateStory(
            @PathVariable String storyKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Story story = storyRepository.findByStoryKey(storyKey)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        if (!story.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("title") != null) {
            story.setTitle((String) request.get("title"));
        }
        if (request.get("description") != null) {
            story.setDescription((String) request.get("description"));
        }
        if (request.get("thumbnail") != null) {
            story.setThumbnail((String) request.get("thumbnail"));
        }
        if (request.get("published") != null) {
            story.setPublished((Boolean) request.get("published"));
        }
        if (request.get("archived") != null) {
            story.setArchived((Boolean) request.get("archived"));
        }
        if (request.get("storyTypeKey") != null) {
            storyTypeRepository.findByTypeKey((String) request.get("storyTypeKey"))
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

    private Map<String, Object> mapToResponse(Story story) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", story.getId());
        response.put("storyKey", story.getStoryKey());
        response.put("title", story.getTitle());
        response.put("description", story.getDescription());
        response.put("thumbnail", story.getThumbnail());
        response.put("published", story.getPublished());
        response.put("archived", story.getArchived());
        if (story.getStoryType() != null) {
            response.put("storyType", Map.of(
                    "typeKey", story.getStoryType().getTypeKey(),
                    "name", story.getStoryType().getName()
            ));
        }
        response.put("createdAt", story.getCreatedAt());
        response.put("updatedAt", story.getUpdatedAt());
        return response;
    }
}

