package com.teckiz.controller;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ResearchJournal;
import com.teckiz.repository.ResearchJournalRepository;
import com.teckiz.service.ModuleAccessManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/journal/admin/journals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class ResearchJournalController {

    private final ModuleAccessManager moduleAccessManager;
    private final ResearchJournalRepository researchJournalRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listJournals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        List<ResearchJournal> journals = researchJournalRepository.findByCompany(
                companyModuleMapper.getCompany()
        );

        // Manual pagination for now
        int start = page * size;
        int end = Math.min(start + size, journals.size());
        List<ResearchJournal> paginatedJournals = journals.subList(start, end);

        List<Map<String, Object>> journalResponses = paginatedJournals.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("journals", journalResponses);
        response.put("totalElements", journals.size());
        response.put("currentPage", page);
        response.put("totalPages", (int) Math.ceil((double) journals.size() / size));
        response.put("leftTab", "journal");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{journalKey}")
    public ResponseEntity<Map<String, Object>> getJournal(@PathVariable String journalKey) {
        moduleAccessManager.authenticateModule();
        
        return researchJournalRepository.findByJournalKey(journalKey)
                .map(journal -> ResponseEntity.ok(mapToResponse(journal)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createJournal(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String name = (String) request.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }

        ResearchJournal journal = ResearchJournal.builder()
                .company(companyModuleMapper.getCompany())
                .name(name)
                .title((String) request.get("title"))
                .subTitle((String) request.get("subTitle"))
                .abbreviation((String) request.get("abbreviation"))
                .printISSN((String) request.get("printISSN"))
                .onlineISSN((String) request.get("onlineISSN"))
                .slug(generateSlug((String) request.getOrDefault("title", name)))
                .publisher((String) request.get("publisher"))
                .faculty((String) request.get("faculty"))
                .startYear((String) request.get("startYear"))
                .onlineStartYear((String) request.get("onlineStartYear"))
                .description((String) request.get("description"))
                .licence((String) request.get("licence"))
                .creativeLicenceType((String) request.getOrDefault("creativeLicenceType", "cc-by"))
                .thumbnail((String) request.get("thumbnail"))
                .submissionActivated(request.get("submissionActivated") != null ? 
                        (Boolean) request.get("submissionActivated") : false)
                .requiredVotes(request.get("requiredVotes") != null ? 
                        ((Number) request.get("requiredVotes")).intValue() : 0)
                .build();

        journal = researchJournalRepository.save(journal);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Journal created successfully", "journalKey", journal.getJournalKey()));
    }

    @PutMapping("/{journalKey}")
    public ResponseEntity<?> updateJournal(
            @PathVariable String journalKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ResearchJournal journal = researchJournalRepository.findByJournalKey(journalKey)
                .orElseThrow(() -> new RuntimeException("Journal not found"));

        if (!journal.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("name") != null) {
            journal.setName((String) request.get("name"));
        }
        if (request.get("title") != null) {
            journal.setTitle((String) request.get("title"));
            journal.setSlug(generateSlug((String) request.get("title")));
        }
        if (request.get("subTitle") != null) {
            journal.setSubTitle((String) request.get("subTitle"));
        }
        if (request.get("abbreviation") != null) {
            journal.setAbbreviation((String) request.get("abbreviation"));
        }
        if (request.get("printISSN") != null) {
            journal.setPrintISSN((String) request.get("printISSN"));
        }
        if (request.get("onlineISSN") != null) {
            journal.setOnlineISSN((String) request.get("onlineISSN"));
        }
        if (request.get("publisher") != null) {
            journal.setPublisher((String) request.get("publisher"));
        }
        if (request.get("faculty") != null) {
            journal.setFaculty((String) request.get("faculty"));
        }
        if (request.get("description") != null) {
            journal.setDescription((String) request.get("description"));
        }
        if (request.get("submissionActivated") != null) {
            journal.setSubmissionActivated((Boolean) request.get("submissionActivated"));
        }
        if (request.get("requiredVotes") != null) {
            journal.setRequiredVotes(((Number) request.get("requiredVotes")).intValue());
        }

        journal = researchJournalRepository.save(journal);

        return ResponseEntity.ok(Map.of("message", "Journal updated successfully"));
    }

    private Map<String, Object> mapToResponse(ResearchJournal journal) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", journal.getId());
        response.put("journalKey", journal.getJournalKey());
        response.put("name", journal.getName());
        response.put("title", journal.getTitle());
        response.put("subTitle", journal.getSubTitle());
        response.put("abbreviation", journal.getAbbreviation());
        response.put("printISSN", journal.getPrintISSN());
        response.put("onlineISSN", journal.getOnlineISSN());
        response.put("slug", journal.getSlug());
        response.put("publisher", journal.getPublisher());
        response.put("faculty", journal.getFaculty());
        response.put("startYear", journal.getStartYear());
        response.put("submissionActivated", journal.getSubmissionActivated());
        response.put("requiredVotes", journal.getRequiredVotes());
        response.put("thumbnail", journal.getThumbnail());
        response.put("createdAt", journal.getCreatedAt());
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

