package com.teckiz.controller.admin.journal;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ResearchArticle;
import com.teckiz.entity.ResearchArticleAuthor;
import com.teckiz.entity.ResearchArticleAuthorMapper;
import com.teckiz.entity.ResearchArticleType;
import com.teckiz.entity.ResearchJournalVolume;
import com.teckiz.repository.ResearchArticleAuthorMapperRepository;
import com.teckiz.repository.ResearchArticleAuthorRepository;
import com.teckiz.repository.ResearchArticleRepository;
import com.teckiz.repository.ResearchArticleTypeRepository;
import com.teckiz.repository.ResearchJournalVolumeRepository;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/journal/admin/articles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class ResearchArticleController {

    private final ModuleAccessManager moduleAccessManager;
    private final ResearchArticleRepository articleRepository;
    private final ResearchArticleTypeRepository articleTypeRepository;
    private final ResearchJournalVolumeRepository volumeRepository;
    private final ResearchArticleAuthorRepository authorRepository;
    private final ResearchArticleAuthorMapperRepository authorMapperRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String articleTypeKey,
            @RequestParam(required = false) String volumeKey,
            @RequestParam(required = false) String searchKey) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ResearchArticle> articles;

        if (status != null && !status.isEmpty()) {
            articles = articleRepository.findByCompanyAndStatus(
                    companyModuleMapper.getCompany(),
                    status,
                    pageable
            );
        } else {
            articles = articleRepository.findByCompany(
                    companyModuleMapper.getCompany(),
                    pageable
            );
        }

        // Filter by article type and volume in memory for now
        // TODO: Move filtering to repository queries for better performance
        List<ResearchArticle> filteredArticles = new ArrayList<>(articles.getContent());
        
        // Filter by article type if provided
        if (articleTypeKey != null && !articleTypeKey.isEmpty()) {
            ResearchArticleType articleType = articleTypeRepository.findByTypeKey(articleTypeKey)
                    .orElse(null);
            if (articleType != null) {
                final Long articleTypeId = articleType.getId();
                filteredArticles = filteredArticles.stream()
                        .filter(article -> article.getResearchArticleType() != null &&
                                article.getResearchArticleType().getId().equals(articleTypeId))
                        .collect(Collectors.toList());
            }
        }

        // Filter by volume if provided
        if (volumeKey != null && !volumeKey.isEmpty()) {
            ResearchJournalVolume volume = volumeRepository.findByVolumeKey(volumeKey)
                    .orElse(null);
            if (volume != null) {
                final Long volumeId = volume.getId();
                filteredArticles = filteredArticles.stream()
                        .filter(article -> article.getResearchJournalVolume() != null &&
                                article.getResearchJournalVolume().getId().equals(volumeId))
                        .collect(Collectors.toList());
            }
        }
        
        // Apply pagination to filtered results
        int start = page * size;
        int end = Math.min(start + size, filteredArticles.size());
        List<ResearchArticle> paginatedArticles = filteredArticles.subList(start, end);

        List<Map<String, Object>> articleResponses = paginatedArticles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("articles", articleResponses);
        response.put("totalPages", (int) Math.ceil((double) filteredArticles.size() / size));
        response.put("totalElements", filteredArticles.size());
        response.put("currentPage", page);
        response.put("leftTab", "article");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{articleKey}")
    public ResponseEntity<Map<String, Object>> getArticle(@PathVariable String articleKey) {
        moduleAccessManager.authenticateModule();
        
        return articleRepository.findByArticleKey(articleKey)
                .map(article -> ResponseEntity.ok(mapToResponse(article)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createArticle(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String title = (String) request.get("title");
        if (title == null || title.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Title is required"));
        }

        ResearchArticle article = ResearchArticle.builder()
                .company(companyModuleMapper.getCompany())
                .title(title)
                .slug(generateSlug(title))
                .englishTitle((String) request.get("englishTitle"))
                .abstractText((String) request.get("abstract"))
                .keywords((String) request.get("keywords"))
                .discipline((String) request.get("discipline"))
                .language((String) request.get("language"))
                .titleLanguage((String) request.getOrDefault("titleLanguage", "eng"))
                .abstractLanguage((String) request.getOrDefault("abstractLanguage", "eng"))
                .keywordsLanguage((String) request.getOrDefault("keywordsLanguage", "eng"))
                .references((String) request.get("references"))
                .pageNumber((String) request.get("pageNumber"))
                .startPage(request.get("startPage") != null ? 
                        ((Number) request.get("startPage")).intValue() : null)
                .endPage(request.get("endPage") != null ? 
                        ((Number) request.get("endPage")).intValue() : null)
                .thumbnail((String) request.get("thumbnail"))
                .status((String) request.getOrDefault("status", ResearchArticle.INCOMPLETE))
                .build();

        // Set article type if provided
        if (request.get("articleTypeKey") != null) {
            articleTypeRepository.findByTypeKey((String) request.get("articleTypeKey"))
                    .ifPresent(article::setResearchArticleType);
        }

        // Set volume if provided
        if (request.get("volumeKey") != null) {
            String volumeKey = (String) request.get("volumeKey");
            ResearchJournalVolume volume = volumeRepository.findByVolumeKey(volumeKey)
                    .orElse(null);
            if (volume != null && volume.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
                article.setResearchJournalVolume(volume);
            }
        }

        article = articleRepository.save(article);

        // Add authors if provided
        if (request.get("authorKeys") != null) {
            @SuppressWarnings("unchecked")
            List<String> authorKeys = (List<String>) request.get("authorKeys");
            addAuthorsToArticle(article, authorKeys);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Article created successfully", "articleKey", article.getArticleKey()));
    }

    @PutMapping("/{articleKey}")
    public ResponseEntity<?> updateArticle(
            @PathVariable String articleKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ResearchArticle article = articleRepository.findByArticleKey(articleKey)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        if (!article.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("title") != null) {
            article.setTitle((String) request.get("title"));
            article.setSlug(generateSlug((String) request.get("title")));
        }
        if (request.get("englishTitle") != null) {
            article.setEnglishTitle((String) request.get("englishTitle"));
        }
        if (request.get("abstract") != null) {
            article.setAbstractText((String) request.get("abstract"));
        }
        if (request.get("keywords") != null) {
            article.setKeywords((String) request.get("keywords"));
        }
        if (request.get("discipline") != null) {
            article.setDiscipline((String) request.get("discipline"));
        }
        if (request.get("language") != null) {
            article.setLanguage((String) request.get("language"));
        }
        if (request.get("references") != null) {
            article.setReferences((String) request.get("references"));
        }
        if (request.get("pageNumber") != null) {
            article.setPageNumber((String) request.get("pageNumber"));
        }
        if (request.get("startPage") != null) {
            article.setStartPage(((Number) request.get("startPage")).intValue());
        }
        if (request.get("endPage") != null) {
            article.setEndPage(((Number) request.get("endPage")).intValue());
        }
        if (request.get("status") != null) {
            String newStatus = (String) request.get("status");
            article.setStatus(newStatus);
            
            // Update timestamps based on status
            if (ResearchArticle.RECEIVED.equals(newStatus) && article.getReceivedAt() == null) {
                article.setReceivedAt(LocalDateTime.now());
            } else if (ResearchArticle.APPROVED.equals(newStatus) && article.getApprovedAt() == null) {
                article.setApprovedAt(LocalDateTime.now());
            }
        }
        if (request.get("published") != null) {
            Boolean published = (Boolean) request.get("published");
            article.setPublished(published);
            if (published && article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now());
            }
        }
        if (request.get("articleTypeKey") != null) {
            articleTypeRepository.findByTypeKey((String) request.get("articleTypeKey"))
                    .ifPresent(article::setResearchArticleType);
        }
        if (request.get("volumeKey") != null) {
            String volumeKey = (String) request.get("volumeKey");
            ResearchJournalVolume volume = volumeRepository.findByVolumeKey(volumeKey)
                    .orElse(null);
            if (volume != null && volume.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
                article.setResearchJournalVolume(volume);
            }
        }

        article = articleRepository.save(article);

        // Update authors if provided
        if (request.get("authorKeys") != null) {
            @SuppressWarnings("unchecked")
            List<String> authorKeys = (List<String>) request.get("authorKeys");
            updateArticleAuthors(article, authorKeys);
        }

        return ResponseEntity.ok(Map.of("message", "Article updated successfully"));
    }

    @DeleteMapping("/{articleKey}")
    public ResponseEntity<?> deleteArticle(@PathVariable String articleKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ResearchArticle article = articleRepository.findByArticleKey(articleKey)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        if (!article.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Soft delete by changing status or archiving
        article.setStatus(ResearchArticle.INCOMPLETE);
        article.setPublished(false);
        articleRepository.save(article);

        return ResponseEntity.ok(Map.of("message", "Article deleted successfully"));
    }

    @PostMapping("/{articleKey}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String articleKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ResearchArticle article = articleRepository.findByArticleKey(articleKey)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        if (!article.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String newStatus = (String) request.get("status");
        if (newStatus == null || newStatus.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Status is required"));
        }

        article.setStatus(newStatus);
        
        // Update timestamps based on status
        if (ResearchArticle.RECEIVED.equals(newStatus) && article.getReceivedAt() == null) {
            article.setReceivedAt(LocalDateTime.now());
        } else if (ResearchArticle.APPROVED.equals(newStatus) && article.getApprovedAt() == null) {
            article.setApprovedAt(LocalDateTime.now());
        }

        article = articleRepository.save(article);

        return ResponseEntity.ok(Map.of("message", "Status updated successfully", "status", article.getStatus()));
    }

    private void addAuthorsToArticle(ResearchArticle article, List<String> authorKeys) {
        List<ResearchArticleAuthorMapper> existingMappers = article.getResearchArticleAuthorMappers();
        if (existingMappers == null) {
            existingMappers = new ArrayList<>();
        }

        int position = existingMappers.size();
        for (String authorKey : authorKeys) {
            ResearchArticleAuthor author = authorRepository.findByAuthorKey(authorKey)
                    .orElse(null);
            
            if (author != null && author.getCompany().getId().equals(article.getCompany().getId())) {
                // Check if author already mapped
                boolean alreadyMapped = existingMappers.stream()
                        .anyMatch(mapper -> mapper.getResearchArticleAuthor().getId().equals(author.getId()) &&
                                !Boolean.TRUE.equals(mapper.getArchived()));

                if (!alreadyMapped) {
                    ResearchArticleAuthorMapper mapper = ResearchArticleAuthorMapper.builder()
                            .researchArticle(article)
                            .researchArticleAuthor(author)
                            .position(position++)
                            .archived(false)
                            .build();
                    authorMapperRepository.save(mapper);
                }
            }
        }
    }

    private void updateArticleAuthors(ResearchArticle article, List<String> authorKeys) {
        // Archive existing mappers
        if (article.getResearchArticleAuthorMappers() != null) {
            article.getResearchArticleAuthorMappers().forEach(mapper -> {
                mapper.setArchived(true);
                authorMapperRepository.save(mapper);
            });
        }

        // Add new authors
        addAuthorsToArticle(article, authorKeys);
    }

    private Map<String, Object> mapToResponse(ResearchArticle article) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", article.getId());
        response.put("articleKey", article.getArticleKey());
        response.put("title", article.getTitle());
        response.put("englishTitle", article.getEnglishTitle());
        response.put("slug", article.getSlug());
        response.put("status", article.getStatus());
        response.put("published", article.getPublished());
        response.put("publishedAt", article.getPublishedAt());
        response.put("receivedAt", article.getReceivedAt());
        response.put("approvedAt", article.getApprovedAt());
        response.put("discipline", article.getDiscipline());
        response.put("language", article.getLanguage());
        response.put("pageNumber", article.getPageNumber());
        response.put("startPage", article.getStartPage());
        response.put("endPage", article.getEndPage());
        response.put("thumbnail", article.getThumbnail());
        response.put("downloads", article.getDownloads());
        response.put("views", article.getViews());
        response.put("visits", article.getVisits());
        
        if (article.getResearchArticleType() != null) {
            response.put("articleType", Map.of(
                    "id", article.getResearchArticleType().getId(),
                    "name", article.getResearchArticleType().getName()
            ));
        }
        
        if (article.getResearchJournalVolume() != null) {
            response.put("volume", Map.of(
                    "id", article.getResearchJournalVolume().getId(),
                    "volumeKey", article.getResearchJournalVolume().getVolumeKey(),
                    "title", article.getResearchJournalVolume().getTitle(),
                    "volumeNumber", article.getResearchJournalVolume().getVolumeNumber()
            ));
        }
        
        // Add authors
        if (article.getResearchArticleAuthorMappers() != null) {
            response.put("authors", article.getResearchArticleAuthorMappers().stream()
                    .filter(mapper -> !Boolean.TRUE.equals(mapper.getArchived()))
                    .sorted((a, b) -> Integer.compare(
                            a.getPosition() != null ? a.getPosition() : 0,
                            b.getPosition() != null ? b.getPosition() : 0))
                    .map(mapper -> {
                        Map<String, Object> authorMap = new HashMap<>();
                        if (mapper.getResearchArticleAuthor() != null) {
                            authorMap.put("authorKey", mapper.getResearchArticleAuthor().getAuthorKey());
                            authorMap.put("name", mapper.getResearchArticleAuthor().getName());
                            authorMap.put("email", mapper.getResearchArticleAuthor().getEmail());
                        }
                        authorMap.put("position", mapper.getPosition());
                        return authorMap;
                    })
                    .collect(Collectors.toList()));
        }
        
        response.put("createdAt", article.getCreatedAt());
        response.put("updatedAt", article.getUpdatedAt());
        return response;
    }

    private String generateSlug(String title) {
        if (title == null) {
            return null;
        }
        return title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}

