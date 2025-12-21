package com.teckiz.controller.publicapi;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.IndexJournal;
import com.teckiz.repository.IndexJournalRepository;
import com.teckiz.service.WebsiteManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Public - IndexJournal", description = "Public API endpoints for IndexJournal")
@RequestMapping("/public/index-journals")
@RequiredArgsConstructor
@org.springframework.stereotype.Component("publicIndexJournalController")
public class IndexJournalController {

    private final WebsiteManager websiteManager;
    private final IndexJournalRepository indexJournalRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listIndexJournals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        if (search != null && !search.isEmpty()) {
            List<IndexJournal> searchResults = indexJournalRepository.findByCompanyAndNameContaining(
                    companyModuleMapper.getCompany(), search);
            // Filter to only active and non-archived
            searchResults = searchResults.stream()
                    .filter(j -> Boolean.TRUE.equals(j.getActive()) && !Boolean.TRUE.equals(j.getArchived()))
                    .toList();
            int start = page * size;
            int end = Math.min(start + size, searchResults.size());
            List<IndexJournal> paginated = start < searchResults.size() ? 
                    searchResults.subList(start, end) : List.of();

            Map<String, Object> response = new HashMap<>();
            response.put("journals", paginated.stream()
                    .map(this::mapToResponse)
                    .toList());
            response.put("totalPages", (int) Math.ceil((double) searchResults.size() / size));
            response.put("totalElements", searchResults.size());
            response.put("currentPage", page);
            return ResponseEntity.ok(response);
        }

        List<IndexJournal> activeList = indexJournalRepository.findByCompanyAndActiveTrueAndArchivedFalse(
                companyModuleMapper.getCompany());
        int start = page * size;
        int end = Math.min(start + size, activeList.size());
        List<IndexJournal> paginated = start < activeList.size() ? 
                activeList.subList(start, end) : List.of();

        Map<String, Object> response = new HashMap<>();
        response.put("journals", paginated.stream()
                .map(this::mapToResponse)
                .toList());
        response.put("totalPages", (int) Math.ceil((double) activeList.size() / size));
        response.put("totalElements", activeList.size());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{journalKey}")
    public ResponseEntity<Map<String, Object>> getIndexJournal(@PathVariable String journalKey) {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        IndexJournal journal = indexJournalRepository.findByJournalKey(journalKey)
                .orElseThrow(() -> new RuntimeException("Index journal not found"));

        if (!journal.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        if (!Boolean.TRUE.equals(journal.getActive()) || Boolean.TRUE.equals(journal.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(journal));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Map<String, Object>> getIndexJournalBySlug(@PathVariable String slug) {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        IndexJournal journal = indexJournalRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Index journal not found"));

        if (!journal.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        if (!Boolean.TRUE.equals(journal.getActive()) || Boolean.TRUE.equals(journal.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(journal));
    }

    private Map<String, Object> mapToResponse(IndexJournal journal) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", journal.getId());
        response.put("journalKey", journal.getJournalKey());
        response.put("name", journal.getName());
        response.put("abbreviation", journal.getAbbreviation());
        response.put("title", journal.getTitle());
        response.put("subTitle", journal.getSubTitle());
        response.put("printISSN", journal.getPrintISSN());
        response.put("onlineISSN", journal.getOnlineISSN());
        response.put("slug", journal.getSlug());
        response.put("thumbnail", journal.getThumbnail());
        response.put("publisher", journal.getPublisher());
        response.put("description", journal.getDescription());
        response.put("active", journal.getActive());
        response.put("createdAt", journal.getCreatedAt());
        response.put("updatedAt", journal.getUpdatedAt());
        return response;
    }
}

