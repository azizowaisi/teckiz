package com.teckiz.controller.publicapi;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.dto.StoryResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Story;
import com.teckiz.repository.StoryRepository;
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
@Tag(name = "Public - Story", description = "Public API endpoints for Story")
@RequestMapping("/public/stories")
@RequiredArgsConstructor
@org.springframework.stereotype.Component("publicStoryController")
public class StoryController {

    private final WebsiteManager websiteManager;
    private final StoryRepository storyRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long storyTypeId) {

        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Story> stories;

        if (storyTypeId != null) {
            stories = storyRepository.findByCompanyAndStoryTypeIdAndPublishedTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(), storyTypeId, pageable);
        } else {
            stories = storyRepository.findByCompanyAndPublishedTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(), pageable);
        }

        List<StoryResponse> responses = stories.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("stories", responses);
        response.put("totalPages", stories.getTotalPages());
        response.put("totalElements", stories.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{storyKey}")
    public ResponseEntity<StoryResponse> getStory(@PathVariable String storyKey) {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        Story story = storyRepository.findByStoryKey(storyKey)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        if (!story.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        if (!Boolean.TRUE.equals(story.getPublished()) || Boolean.TRUE.equals(story.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(story));
    }

    private StoryResponse mapToResponse(Story story) {
        return StoryResponse.builder()
                .id(story.getId())
                .storyKey(story.getStoryKey())
                .title(story.getTitle())
                .description(story.getDescription())
                .thumbnail(story.getThumbnail())
                .published(story.getPublished())
                .archived(story.getArchived())
                .storyTypeId(story.getStoryType() != null ? story.getStoryType().getId() : null)
                .storyTypeName(story.getStoryType() != null ? story.getStoryType().getName() : null)
                .companyId(story.getCompany().getId())
                .companyName(story.getCompany().getName())
                .createdAt(story.getCreatedAt())
                .updatedAt(story.getUpdatedAt())
                .build();
    }
}

