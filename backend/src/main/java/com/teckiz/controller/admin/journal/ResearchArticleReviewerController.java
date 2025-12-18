package com.teckiz.controller.admin.journal;

import com.teckiz.entity.Company;
import com.teckiz.entity.ResearchArticle;
import com.teckiz.entity.ResearchArticleReviewerMapper;
import com.teckiz.entity.UserCompanyRole;
import com.teckiz.repository.ResearchArticleRepository;
import com.teckiz.repository.ResearchArticleReviewerMapperRepository;
import com.teckiz.repository.UserCompanyRoleRepository;
import com.teckiz.service.ModuleAccessManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/journal/admin/reviewers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class ResearchArticleReviewerController {

    private final ModuleAccessManager moduleAccessManager;
    private final ResearchArticleReviewerMapperRepository reviewerMapperRepository;
    private final ResearchArticleRepository articleRepository;
    private final UserCompanyRoleRepository userCompanyRoleRepository;

    @GetMapping("/articles/{articleKey}")
    public ResponseEntity<Map<String, Object>> getArticleReviewers(@PathVariable String articleKey) {
        Company company = moduleAccessManager.authenticateModule().getCompany();

        ResearchArticle article = articleRepository.findByArticleKey(articleKey)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        if (!article.getCompany().getId().equals(company.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ResearchArticleReviewerMapper> reviewers = reviewerMapperRepository.findByArticle(article);

        List<Map<String, Object>> reviewerResponses = reviewers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("reviewers", reviewerResponses);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/articles/{articleKey}/assign")
    public ResponseEntity<?> assignReviewer(
            @PathVariable String articleKey,
            @RequestBody Map<String, Object> request) {

        Company company = moduleAccessManager.authenticateModule().getCompany();

        ResearchArticle article = articleRepository.findByArticleKey(articleKey)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        if (!article.getCompany().getId().equals(company.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String userCompanyRoleId = (String) request.get("userCompanyRoleId");
        if (userCompanyRoleId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User company role ID is required"));
        }

        UserCompanyRole reviewer = userCompanyRoleRepository.findById(Long.parseLong(userCompanyRoleId))
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        if (!reviewer.getCompany().getId().equals(company.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Check if reviewer already assigned
        List<ResearchArticleReviewerMapper> existingMappers = reviewerMapperRepository.findByArticle(article);
        boolean alreadyAssigned = existingMappers.stream()
                .anyMatch(mapper -> mapper.getReviewer() != null &&
                        mapper.getReviewer().getId().equals(reviewer.getId()));

        if (alreadyAssigned) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Reviewer already assigned to this article"));
        }

        ResearchArticleReviewerMapper mapper = ResearchArticleReviewerMapper.builder()
                .article(article)
                .reviewer(reviewer)
                .status(ResearchArticleReviewerMapper.PENDING)
                .acceptedForReview("no")
                .reviewSubmitted("no")
                .build();

        if (request.get("reviewDueAt") != null) {
            mapper.setReviewDueAt(LocalDateTime.parse((String) request.get("reviewDueAt")));
        }

        mapper = reviewerMapperRepository.save(mapper);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Reviewer assigned successfully", "mapperKey", mapper.getMapperKey()));
    }

    @PutMapping("/{mapperKey}/status")
    public ResponseEntity<?> updateReviewerStatus(
            @PathVariable String mapperKey,
            @RequestBody Map<String, Object> request) {

        Company company = moduleAccessManager.authenticateModule().getCompany();

        ResearchArticleReviewerMapper mapper = reviewerMapperRepository.findByMapperKey(mapperKey)
                .orElseThrow(() -> new RuntimeException("Reviewer mapper not found"));

        if (!mapper.getArticle().getCompany().getId().equals(company.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("status") != null) {
            mapper.setStatus((String) request.get("status"));
        }
        if (request.get("acceptedForReview") != null) {
            mapper.setAcceptedForReview((String) request.get("acceptedForReview"));
        }
        if (request.get("reviewSubmitted") != null) {
            mapper.setReviewSubmitted((String) request.get("reviewSubmitted"));
        }

        mapper = reviewerMapperRepository.save(mapper);

        return ResponseEntity.ok(Map.of("message", "Reviewer status updated successfully"));
    }

    @DeleteMapping("/{mapperKey}")
    public ResponseEntity<?> removeReviewer(@PathVariable String mapperKey) {
        Company company = moduleAccessManager.authenticateModule().getCompany();

        ResearchArticleReviewerMapper mapper = reviewerMapperRepository.findByMapperKey(mapperKey)
                .orElseThrow(() -> new RuntimeException("Reviewer mapper not found"));

        if (!mapper.getArticle().getCompany().getId().equals(company.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        reviewerMapperRepository.delete(mapper);

        return ResponseEntity.ok(Map.of("message", "Reviewer removed successfully"));
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("hasAnyRole('COMPANY_REVIEWER')")
    public ResponseEntity<Map<String, Object>> getMyReviews(
            @RequestParam(required = false) String status) {

        // Get current user's company role
        // This would need to be implemented based on your authentication setup
        // For now, returning empty list
        Map<String, Object> response = new HashMap<>();
        response.put("reviews", List.of());
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> mapToResponse(ResearchArticleReviewerMapper mapper) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", mapper.getId());
        response.put("mapperKey", mapper.getMapperKey());
        response.put("status", mapper.getStatus());
        response.put("acceptedForReview", mapper.getAcceptedForReview());
        response.put("reviewSubmitted", mapper.getReviewSubmitted());
        response.put("reviewDueAt", mapper.getReviewDueAt());
        response.put("responseDueAt", mapper.getResponseDueAt());
        response.put("createdAt", mapper.getCreatedAt());
        response.put("updatedAt", mapper.getUpdatedAt());

        if (mapper.getArticle() != null) {
            response.put("article", Map.of(
                    "articleKey", mapper.getArticle().getArticleKey(),
                    "title", mapper.getArticle().getTitle()
            ));
        }

        if (mapper.getReviewer() != null && mapper.getReviewer().getUser() != null) {
            response.put("reviewer", Map.of(
                    "id", mapper.getReviewer().getId(),
                    "name", mapper.getReviewer().getUser().getName()
            ));
        }

        return response;
    }
}

