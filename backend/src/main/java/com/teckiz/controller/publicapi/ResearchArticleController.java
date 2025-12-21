package com.teckiz.controller.publicapi;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ResearchArticle;
import com.teckiz.entity.ResearchJournalVolume;
import com.teckiz.repository.ResearchArticleRepository;
import com.teckiz.repository.ResearchJournalVolumeRepository;
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
@Tag(name = "Public - ResearchArticle", description = "Public API endpoints for ResearchArticle")
@RequestMapping("/public/journal/articles")
@RequiredArgsConstructor
@org.springframework.stereotype.Component("publicResearchArticleController")
public class ResearchArticleController {

    private final WebsiteManager websiteManager;
    private final ResearchArticleRepository articleRepository;
    private final ResearchJournalVolumeRepository volumeRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listPublishedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String volumeKey,
            @RequestParam(required = false) String search) {

        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<ResearchArticle> articles;

        if (volumeKey != null && !volumeKey.isEmpty()) {
            ResearchJournalVolume volume = volumeRepository.findByVolumeKey(volumeKey)
                    .orElseThrow(() -> new RuntimeException("Volume not found"));
            articles = articleRepository.findByResearchJournalVolume(volume, pageable);
        } else {
            articles = articleRepository.findByCompanyAndPublishedTrue(
                    companyModuleMapper.getCompany(),
                    pageable
            );
        }

        // Filter by search if provided
        if (search != null && !search.isEmpty()) {
            List<ResearchArticle> searchResults = articleRepository.searchPublishedArticles(
                    companyModuleMapper.getCompany(),
                    search
            );
            // Manual pagination for search results
            int start = page * size;
            int end = Math.min(start + size, searchResults.size());
            List<ResearchArticle> paginatedResults = searchResults.subList(start, end);
            
            List<Map<String, Object>> articleResponses = paginatedResults.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("articles", articleResponses);
            response.put("totalElements", searchResults.size());
            response.put("currentPage", page);
            response.put("totalPages", (int) Math.ceil((double) searchResults.size() / size));
            return ResponseEntity.ok(response);
        }

        List<Map<String, Object>> articleResponses = articles.getContent().stream()
                .filter(article -> Boolean.TRUE.equals(article.getPublished()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("articles", articleResponses);
        response.put("totalPages", articles.getTotalPages());
        response.put("totalElements", articles.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Map<String, Object>> getArticleBySlug(@PathVariable String slug) {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        ResearchArticle article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        // Verify article belongs to company and is published
        if (!article.getCompany().getId().equals(companyModuleMapper.getCompany().getId()) ||
                !Boolean.TRUE.equals(article.getPublished())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(article));
    }

    private Map<String, Object> mapToResponse(ResearchArticle article) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", article.getId());
        response.put("articleKey", article.getArticleKey());
        response.put("title", article.getTitle());
        response.put("englishTitle", article.getEnglishTitle());
        response.put("slug", article.getSlug());
        response.put("abstract", article.getAbstractText());
        response.put("keywords", article.getKeywords());
        response.put("discipline", article.getDiscipline());
        response.put("language", article.getLanguage());
        response.put("pageNumber", article.getPageNumber());
        response.put("startPage", article.getStartPage());
        response.put("endPage", article.getEndPage());
        response.put("publishedAt", article.getPublishedAt());
        response.put("thumbnail", article.getThumbnail());
        
        // Add authors
        if (article.getResearchArticleAuthorMappers() != null) {
            response.put("authors", article.getResearchArticleAuthorMappers().stream()
                    .filter(mapper -> !Boolean.TRUE.equals(mapper.getArchived()))
                    .map(mapper -> {
                        Map<String, Object> authorMap = new HashMap<>();
                        if (mapper.getResearchArticleAuthor() != null) {
                            authorMap.put("name", mapper.getResearchArticleAuthor().getName());
                            authorMap.put("email", mapper.getResearchArticleAuthor().getEmail());
                            authorMap.put("orcid", mapper.getResearchArticleAuthor().getOrcid());
                        }
                        authorMap.put("position", mapper.getPosition());
                        return authorMap;
                    })
                    .collect(Collectors.toList()));
        }
        
        // Add volume info
        if (article.getResearchJournalVolume() != null) {
            Map<String, Object> volumeMap = new HashMap<>();
            volumeMap.put("id", article.getResearchJournalVolume().getId());
            volumeMap.put("title", article.getResearchJournalVolume().getTitle());
            volumeMap.put("volumeNumber", article.getResearchJournalVolume().getVolumeNumber());
            volumeMap.put("issueNumber", article.getResearchJournalVolume().getIssueNumber());
            response.put("volume", volumeMap);
        }
        
        response.put("createdAt", article.getCreatedAt());
        return response;
    }
}

