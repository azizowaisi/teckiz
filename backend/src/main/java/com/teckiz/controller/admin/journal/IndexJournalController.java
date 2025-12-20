package com.teckiz.controller.admin.journal;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.IndexJournal;
import com.teckiz.repository.IndexJournalRepository;
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
@RequestMapping("/journal/admin/index-journals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class IndexJournalController {

    private final ModuleAccessManager moduleAccessManager;
    private final IndexJournalRepository indexJournalRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listIndexJournals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<IndexJournal> journals;

        if (search != null && !search.isEmpty()) {
            List<IndexJournal> searchResults = indexJournalRepository.findByCompanyAndNameContaining(
                    companyModuleMapper.getCompany(), search);
            int start = page * size;
            int end = Math.min(start + size, searchResults.size());
            List<IndexJournal> paginated = searchResults.subList(start, end);

            Map<String, Object> response = new HashMap<>();
            response.put("journals", paginated.stream()
                    .map(this::mapToResponse)
                    .toList());
            response.put("totalPages", (int) Math.ceil((double) searchResults.size() / size));
            response.put("totalElements", searchResults.size());
            response.put("currentPage", page);
            return ResponseEntity.ok(response);
        }

        if (active != null && active) {
            List<IndexJournal> activeList = indexJournalRepository.findByCompanyAndActiveTrueAndArchivedFalse(
                    companyModuleMapper.getCompany());
            int start = page * size;
            int end = Math.min(start + size, activeList.size());
            List<IndexJournal> paginated = activeList.subList(start, end);

            Map<String, Object> response = new HashMap<>();
            response.put("journals", paginated.stream()
                    .map(this::mapToResponse)
                    .toList());
            response.put("totalPages", (int) Math.ceil((double) activeList.size() / size));
            response.put("totalElements", activeList.size());
            response.put("currentPage", page);
            return ResponseEntity.ok(response);
        } else {
            journals = indexJournalRepository.findByCompanyAndArchivedFalse(
                    companyModuleMapper.getCompany(), pageable);
        }

        List<Map<String, Object>> journalResponses = journals.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("journals", journalResponses);
        response.put("totalPages", journals.getTotalPages());
        response.put("totalElements", journals.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{journalKey}")
    public ResponseEntity<Map<String, Object>> getIndexJournal(@PathVariable String journalKey) {
        moduleAccessManager.authenticateModule();

        return indexJournalRepository.findByJournalKey(journalKey)
                .map(journal -> ResponseEntity.ok(mapToResponse(journal)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createIndexJournal(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String name = (String) request.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }

        String slug = (String) request.get("slug");
        if (slug == null || slug.isEmpty()) {
            slug = UtilHelper.generateSlug(name);
        }

        IndexJournal journal = IndexJournal.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .name(name)
                .abbreviation((String) request.get("abbreviation"))
                .title((String) request.get("title"))
                .subTitle((String) request.get("subTitle"))
                .printISSN((String) request.get("printISSN"))
                .onlineISSN((String) request.get("onlineISSN"))
                .slug(slug)
                .thumbnail((String) request.get("thumbnail"))
                .publisher((String) request.get("publisher"))
                .description((String) request.get("description"))
                .active(request.get("active") != null ? (Boolean) request.get("active") : true)
                .archived(false)
                .build();

        journal = indexJournalRepository.save(journal);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Index journal created successfully", "journalKey", journal.getJournalKey()));
    }

    @PutMapping("/{journalKey}")
    public ResponseEntity<?> updateIndexJournal(
            @PathVariable String journalKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        IndexJournal journal = indexJournalRepository.findByJournalKey(journalKey)
                .orElseThrow(() -> new RuntimeException("Index journal not found"));

        if (!journal.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("name") != null) {
            journal.setName((String) request.get("name"));
        }
        if (request.get("abbreviation") != null) {
            journal.setAbbreviation((String) request.get("abbreviation"));
        }
        if (request.get("title") != null) {
            journal.setTitle((String) request.get("title"));
        }
        if (request.get("subTitle") != null) {
            journal.setSubTitle((String) request.get("subTitle"));
        }
        if (request.get("printISSN") != null) {
            journal.setPrintISSN((String) request.get("printISSN"));
        }
        if (request.get("onlineISSN") != null) {
            journal.setOnlineISSN((String) request.get("onlineISSN"));
        }
        if (request.get("slug") != null) {
            journal.setSlug((String) request.get("slug"));
        }
        if (request.get("thumbnail") != null) {
            journal.setThumbnail((String) request.get("thumbnail"));
        }
        if (request.get("publisher") != null) {
            journal.setPublisher((String) request.get("publisher"));
        }
        if (request.get("description") != null) {
            journal.setDescription((String) request.get("description"));
        }
        if (request.get("active") != null) {
            journal.setActive((Boolean) request.get("active"));
        }
        if (request.get("archived") != null) {
            journal.setArchived((Boolean) request.get("archived"));
        }

        journal = indexJournalRepository.save(journal);

        return ResponseEntity.ok(Map.of("message", "Index journal updated successfully"));
    }

    @DeleteMapping("/{journalKey}")
    public ResponseEntity<?> deleteIndexJournal(@PathVariable String journalKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        IndexJournal journal = indexJournalRepository.findByJournalKey(journalKey)
                .orElseThrow(() -> new RuntimeException("Index journal not found"));

        if (!journal.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        journal.setArchived(true);
        indexJournalRepository.save(journal);

        return ResponseEntity.ok(Map.of("message", "Index journal deleted successfully"));
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
        response.put("archived", journal.getArchived());
        response.put("createdAt", journal.getCreatedAt());
        response.put("updatedAt", journal.getUpdatedAt());
        return response;
    }
}

