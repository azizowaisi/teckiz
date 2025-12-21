package com.teckiz.controller.publicapi;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebNews;
import com.teckiz.repository.WebNewsRepository;
import com.teckiz.service.WebsiteManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Public - WebNews", description = "Public API endpoints for WebNews")
@RequestMapping("/public/news")
@RequiredArgsConstructor
@org.springframework.stereotype.Component("publicWebNewsController")
public class WebNewsController {

    private final WebsiteManager websiteManager;
    private final WebNewsRepository webNewsRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listPublishedNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long newsTypeId) {

        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        List<WebNews> publishedNews = webNewsRepository.findPublishedNews(
                companyModuleMapper.getCompany(),
                LocalDateTime.now()
        );

        // Apply pagination manually for now (can be optimized with custom query)
        int start = page * size;
        int end = Math.min(start + size, publishedNews.size());
        List<WebNews> paginatedNews = publishedNews.subList(start, end);

        List<Map<String, Object>> newsResponses = paginatedNews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("news", newsResponses);
        response.put("totalElements", publishedNews.size());
        response.put("currentPage", page);
        response.put("totalPages", (int) Math.ceil((double) publishedNews.size() / size));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Map<String, Object>> getNewsBySlug(@PathVariable String slug) {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        WebNews news = webNewsRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("News not found"));

        // Verify news belongs to company and is published
        if (!news.getCompany().getId().equals(companyModuleMapper.getCompany().getId()) ||
                !Boolean.TRUE.equals(news.getPublished()) ||
                Boolean.TRUE.equals(news.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(news));
    }

    @GetMapping("/carousel")
    public ResponseEntity<List<Map<String, Object>>> getCarouselNews() {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        List<WebNews> carouselNews = webNewsRepository.findByCompanyAndCarouselTrueAndPublishedTrue(
                companyModuleMapper.getCompany()
        );

        List<Map<String, Object>> newsResponses = carouselNews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(newsResponses);
    }

    private Map<String, Object> mapToResponse(WebNews news) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", news.getId());
        response.put("newsKey", news.getNewsKey());
        response.put("title", news.getTitle());
        response.put("slug", news.getSlug());
        response.put("shortDescription", news.getShortDescription());
        response.put("description", news.getDescription());
        response.put("publishedAt", news.getPublishedAt());
        response.put("carousel", news.getCarousel());
        response.put("thumbnail", news.getPoster() != null ? news.getPoster().getLocation() : null);
        if (news.getWebNewsType() != null) {
            response.put("newsType", Map.of(
                    "id", news.getWebNewsType().getId(),
                    "name", news.getWebNewsType().getName()
            ));
        }
        response.put("createdAt", news.getCreatedAt());
        return response;
    }
}

