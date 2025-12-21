package com.teckiz.controller.admin.journal;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.IndexJournal;
import com.teckiz.entity.IndexJournalArticle;
import com.teckiz.entity.IndexJournalVolume;
import com.teckiz.repository.IndexJournalArticleRepository;
import com.teckiz.repository.IndexJournalRepository;
import com.teckiz.repository.IndexJournalVolumeRepository;
import com.teckiz.service.ModuleAccessManager;
import com.teckiz.util.UtilHelper;
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
@RequestMapping("/journal/admin/index-journals/{journalKey}/articles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class IndexJournalArticleController {

    private final ModuleAccessManager moduleAccessManager;
    private final IndexJournalRepository indexJournalRepository;
    private final IndexJournalVolumeRepository volumeRepository;
    private final IndexJournalArticleRepository articleRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listArticles(
            @PathVariable String journalKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) String volumeKey) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        IndexJournal journal = indexJournalRepository.findByJournalKey(journalKey)
                .orElseThrow(() -> new RuntimeException("Index journal not found"));

        if (!journal.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<IndexJournalArticle> articles;

        if (volumeKey != null && !volumeKey.isEmpty()) {
            IndexJournalVolume volume = volumeRepository.findByVolumeKey(volumeKey)
                    .orElse(null);
            if (volume != null) {
                if (published != null && published) {
                    articles = articleRepository.findByCompanyAndIndexJournalVolumeAndPublishedTrueAndArchivedFalse(
                            companyModuleMapper.getCompany(), volume, pageable);
                } else {
                    List<IndexJournalArticle> articleList = articleRepository.findByIndexJournalVolume(volume);
                    int start = page * size;
                    int end = Math.min(start + size, articleList.size());
                    List<IndexJournalArticle> paginated = articleList.subList(start, end);

                    Map<String, Object> response = new HashMap<>();
                    response.put("articles", paginated.stream()
                            .map(this::mapToResponse)
                            .toList());
                    response.put("totalPages", (int) Math.ceil((double) articleList.size() / size));
                    response.put("totalElements", articleList.size());
                    response.put("currentPage", page);
                    return ResponseEntity.ok(response);
                }
            } else {
                articles = Page.empty(pageable);
            }
        } else {
            if (published != null && published) {
                articles = articleRepository.findByCompanyAndPublishedTrueAndArchivedFalse(
                        companyModuleMapper.getCompany(), pageable);
            } else {
                articles = articleRepository.findByCompany(companyModuleMapper.getCompany(), pageable);
            }
        }

        List<Map<String, Object>> articleResponses = articles.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("articles", articleResponses);
        response.put("totalPages", articles.getTotalPages());
        response.put("totalElements", articles.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{articleKey}")
    public ResponseEntity<Map<String, Object>> getArticle(
            @PathVariable String journalKey,
            @PathVariable String articleKey) {

        moduleAccessManager.authenticateModule();

        return articleRepository.findByArticleKey(articleKey)
                .map(article -> ResponseEntity.ok(mapToResponse(article)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createArticle(
            @PathVariable String journalKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        IndexJournal journal = indexJournalRepository.findByJournalKey(journalKey)
                .orElseThrow(() -> new RuntimeException("Index journal not found"));

        if (!journal.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String title = (String) request.get("title");
        if (title == null || title.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Title is required"));
        }

        String slug = (String) request.get("slug");
        if (slug == null || slug.isEmpty()) {
            slug = UtilHelper.generateSlug(title);
        }

        IndexJournalArticle.IndexJournalArticleBuilder builder = IndexJournalArticle.builder()
                .company(companyModuleMapper.getCompany())
                .indexJournal(journal)
                .title(title)
                .subTitle((String) request.get("subTitle"))
                .description((String) request.get("description"))
                .abstractText((String) request.get("abstractText"))
                .keywords((String) request.get("keywords"))
                .doi((String) request.get("doi"))
                .pageStart(request.get("pageStart") != null ? ((Number) request.get("pageStart")).intValue() : null)
                .pageEnd(request.get("pageEnd") != null ? ((Number) request.get("pageEnd")).intValue() : null)
                .slug(slug)
                .thumbnail((String) request.get("thumbnail"))
                .published(request.get("published") != null ? (Boolean) request.get("published") : false)
                .archived(false);

        // Set volume if provided
        if (request.get("volumeKey") != null) {
            volumeRepository.findByVolumeKey((String) request.get("volumeKey"))
                    .ifPresent(builder::indexJournalVolume);
        }

        IndexJournalArticle article = builder.build();
        article = articleRepository.save(article);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Article created successfully", "articleKey", article.getArticleKey()));
    }

    @PutMapping("/{articleKey}")
    public ResponseEntity<?> updateArticle(
            @PathVariable String journalKey,
            @PathVariable String articleKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        IndexJournalArticle article = articleRepository.findByArticleKey(articleKey)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        if (!article.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("title") != null) {
            article.setTitle((String) request.get("title"));
        }
        if (request.get("subTitle") != null) {
            article.setSubTitle((String) request.get("subTitle"));
        }
        if (request.get("description") != null) {
            article.setDescription((String) request.get("description"));
        }
        if (request.get("abstractText") != null) {
            article.setAbstractText((String) request.get("abstractText"));
        }
        if (request.get("keywords") != null) {
            article.setKeywords((String) request.get("keywords"));
        }
        if (request.get("doi") != null) {
            article.setDoi((String) request.get("doi"));
        }
        if (request.get("pageStart") != null) {
            article.setPageStart(((Number) request.get("pageStart")).intValue());
        }
        if (request.get("pageEnd") != null) {
            article.setPageEnd(((Number) request.get("pageEnd")).intValue());
        }
        if (request.get("slug") != null) {
            article.setSlug((String) request.get("slug"));
        }
        if (request.get("thumbnail") != null) {
            article.setThumbnail((String) request.get("thumbnail"));
        }
        if (request.get("published") != null) {
            article.setPublished((Boolean) request.get("published"));
        }
        if (request.get("archived") != null) {
            article.setArchived((Boolean) request.get("archived"));
        }
        if (request.get("volumeKey") != null) {
            volumeRepository.findByVolumeKey((String) request.get("volumeKey"))
                    .ifPresent(article::setIndexJournalVolume);
        }

        article = articleRepository.save(article);

        return ResponseEntity.ok(Map.of("message", "Article updated successfully"));
    }

    @DeleteMapping("/{articleKey}")
    public ResponseEntity<?> deleteArticle(
            @PathVariable String journalKey,
            @PathVariable String articleKey) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        IndexJournalArticle article = articleRepository.findByArticleKey(articleKey)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        if (!article.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        article.setArchived(true);
        articleRepository.save(article);

        return ResponseEntity.ok(Map.of("message", "Article deleted successfully"));
    }

    private Map<String, Object> mapToResponse(IndexJournalArticle article) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", article.getId());
        response.put("articleKey", article.getArticleKey());
        response.put("title", article.getTitle());
        response.put("subTitle", article.getSubTitle());
        response.put("description", article.getDescription());
        response.put("abstractText", article.getAbstractText());
        response.put("keywords", article.getKeywords());
        response.put("doi", article.getDoi());
        response.put("pageStart", article.getPageStart());
        response.put("pageEnd", article.getPageEnd());
        response.put("slug", article.getSlug());
        response.put("thumbnail", article.getThumbnail());
        response.put("published", article.getPublished());
        response.put("publishedAt", article.getPublishedAt());
        response.put("archived", article.getArchived());
        if (article.getIndexJournalVolume() != null) {
            response.put("volumeKey", article.getIndexJournalVolume().getVolumeKey());
            response.put("volumeTitle", article.getIndexJournalVolume().getTitle());
        }
        response.put("createdAt", article.getCreatedAt());
        response.put("updatedAt", article.getUpdatedAt());
        return response;
    }
}

