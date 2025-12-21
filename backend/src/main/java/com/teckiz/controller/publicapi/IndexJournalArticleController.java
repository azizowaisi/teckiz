package com.teckiz.controller.publicapi;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.IndexJournal;
import com.teckiz.entity.IndexJournalArticle;
import com.teckiz.entity.IndexJournalVolume;
import com.teckiz.repository.IndexJournalArticleRepository;
import com.teckiz.repository.IndexJournalRepository;
import com.teckiz.repository.IndexJournalVolumeRepository;
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
@Tag(name = "Public - IndexJournalArticle", description = "Public API endpoints for IndexJournalArticle")
@RequestMapping("/public/index-journals/{journalKey}/articles")
@RequiredArgsConstructor
@org.springframework.stereotype.Component("publicIndexJournalArticleController")
public class IndexJournalArticleController {

    private final WebsiteManager websiteManager;
    private final IndexJournalRepository indexJournalRepository;
    private final IndexJournalVolumeRepository volumeRepository;
    private final IndexJournalArticleRepository articleRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listArticles(
            @PathVariable String journalKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String volumeKey) {

        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        IndexJournal journal = indexJournalRepository.findByJournalKey(journalKey)
                .orElseThrow(() -> new RuntimeException("Index journal not found"));

        if (!journal.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        if (!Boolean.TRUE.equals(journal.getActive()) || Boolean.TRUE.equals(journal.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<IndexJournalArticle> articles;

        if (volumeKey != null && !volumeKey.isEmpty()) {
            IndexJournalVolume volume = volumeRepository.findByVolumeKey(volumeKey)
                    .orElse(null);
            if (volume != null && volume.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
                articles = articleRepository.findByCompanyAndIndexJournalVolumeAndPublishedTrueAndArchivedFalse(
                        companyModuleMapper.getCompany(), volume, pageable);
            } else {
                articles = Page.empty(pageable);
            }
        } else {
            articles = articleRepository.findByCompanyAndIndexJournalAndPublishedTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(), journal, pageable);
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

        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        IndexJournal journal = indexJournalRepository.findByJournalKey(journalKey)
                .orElseThrow(() -> new RuntimeException("Index journal not found"));

        if (!journal.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        IndexJournalArticle article = articleRepository.findByArticleKey(articleKey)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        if (!article.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        if (!Boolean.TRUE.equals(article.getPublished()) || Boolean.TRUE.equals(article.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(article));
    }

    private Map<String, Object> mapToResponse(IndexJournalArticle article) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", article.getId());
        response.put("articleKey", article.getArticleKey());
        response.put("title", article.getTitle());
        response.put("abstract", article.getAbstractText());
        response.put("keywords", article.getKeywords());
        response.put("doi", article.getDoi());
        response.put("pageStart", article.getPageStart());
        response.put("pageEnd", article.getPageEnd());
        response.put("slug", article.getSlug());
        response.put("thumbnail", article.getThumbnail());
        response.put("published", article.getPublished());
        response.put("publishedAt", article.getPublishedAt());
        response.put("indexJournalId", article.getIndexJournal() != null ? article.getIndexJournal().getId() : null);
        response.put("indexJournalName", article.getIndexJournal() != null ? article.getIndexJournal().getName() : null);
        response.put("indexJournalVolumeId", article.getIndexJournalVolume() != null ? article.getIndexJournalVolume().getId() : null);
        response.put("indexJournalVolumeTitle", article.getIndexJournalVolume() != null ? article.getIndexJournalVolume().getTitle() : null);
        response.put("createdAt", article.getCreatedAt());
        response.put("updatedAt", article.getUpdatedAt());
        return response;
    }
}

