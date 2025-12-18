package com.teckiz.controller;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ResearchJournal;
import com.teckiz.entity.ResearchJournalVolume;
import com.teckiz.repository.ResearchJournalRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/journal/admin/volumes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class ResearchJournalVolumeController {

    private final ModuleAccessManager moduleAccessManager;
    private final ResearchJournalVolumeRepository volumeRepository;
    private final ResearchJournalRepository journalRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listVolumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String journalKey) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("volumeNumber").descending());
        Page<ResearchJournalVolume> volumes;

        if (journalKey != null && !journalKey.isEmpty()) {
            ResearchJournal journal = journalRepository.findByJournalKey(journalKey)
                    .orElseThrow(() -> new RuntimeException("Journal not found"));
            volumes = volumeRepository.findByResearchJournal(journal, pageable);
        } else {
            volumes = volumeRepository.findByCompany(companyModuleMapper.getCompany(), pageable);
        }

        List<Map<String, Object>> volumeResponses = volumes.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("volumes", volumeResponses);
        response.put("totalPages", volumes.getTotalPages());
        response.put("totalElements", volumes.getTotalElements());
        response.put("currentPage", page);
        response.put("leftTab", "volume");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{volumeKey}")
    public ResponseEntity<Map<String, Object>> getVolume(@PathVariable String volumeKey) {
        moduleAccessManager.authenticateModule();
        
        return volumeRepository.findByVolumeKey(volumeKey)
                .map(volume -> ResponseEntity.ok(mapToResponse(volume)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createVolume(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String journalKey = (String) request.get("journalKey");
        if (journalKey == null || journalKey.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Journal key is required"));
        }

        ResearchJournal journal = journalRepository.findByJournalKey(journalKey)
                .orElseThrow(() -> new RuntimeException("Journal not found"));

        if (!journal.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ResearchJournalVolume volume = ResearchJournalVolume.builder()
                .company(companyModuleMapper.getCompany())
                .researchJournal(journal)
                .title((String) request.get("title"))
                .subTitle((String) request.get("subTitle"))
                .slug(generateSlug((String) request.getOrDefault("title", "")))
                .volumeNumber(request.get("volumeNumber") != null ? 
                        ((Number) request.get("volumeNumber")).intValue() : null)
                .issueNumber(request.get("issueNumber") != null ? 
                        ((Number) request.get("issueNumber")).intValue() : null)
                .number(request.get("number") != null ? 
                        ((Number) request.get("number")).intValue() : null)
                .description((String) request.get("description"))
                .publisher((String) request.get("publisher"))
                .language((String) request.get("language"))
                .thumbnail((String) request.get("thumbnail"))
                .published(request.get("published") != null ? 
                        (Boolean) request.get("published") : false)
                .publishedAt(request.get("published") != null && (Boolean) request.get("published") ? 
                        LocalDateTime.now() : null)
                .build();

        volume = volumeRepository.save(volume);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Volume created successfully", "volumeKey", volume.getVolumeKey()));
    }

    @PutMapping("/{volumeKey}")
    public ResponseEntity<?> updateVolume(
            @PathVariable String volumeKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ResearchJournalVolume volume = volumeRepository.findByVolumeKey(volumeKey)
                .orElseThrow(() -> new RuntimeException("Volume not found"));

        if (!volume.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("title") != null) {
            volume.setTitle((String) request.get("title"));
            volume.setSlug(generateSlug((String) request.get("title")));
        }
        if (request.get("subTitle") != null) {
            volume.setSubTitle((String) request.get("subTitle"));
        }
        if (request.get("volumeNumber") != null) {
            volume.setVolumeNumber(((Number) request.get("volumeNumber")).intValue());
        }
        if (request.get("issueNumber") != null) {
            volume.setIssueNumber(((Number) request.get("issueNumber")).intValue());
        }
        if (request.get("description") != null) {
            volume.setDescription((String) request.get("description"));
        }
        if (request.get("published") != null) {
            volume.setPublished((Boolean) request.get("published"));
            if ((Boolean) request.get("published") && volume.getPublishedAt() == null) {
                volume.setPublishedAt(LocalDateTime.now());
            }
        }

        volume = volumeRepository.save(volume);

        return ResponseEntity.ok(Map.of("message", "Volume updated successfully"));
    }

    @DeleteMapping("/{volumeKey}")
    public ResponseEntity<?> deleteVolume(@PathVariable String volumeKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        ResearchJournalVolume volume = volumeRepository.findByVolumeKey(volumeKey)
                .orElseThrow(() -> new RuntimeException("Volume not found"));

        if (!volume.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        volume.setArchived(true);
        volumeRepository.save(volume);

        return ResponseEntity.ok(Map.of("message", "Volume archived successfully"));
    }

    private Map<String, Object> mapToResponse(ResearchJournalVolume volume) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", volume.getId());
        response.put("volumeKey", volume.getVolumeKey());
        response.put("title", volume.getTitle());
        response.put("subTitle", volume.getSubTitle());
        response.put("slug", volume.getSlug());
        response.put("volumeNumber", volume.getVolumeNumber());
        response.put("issueNumber", volume.getIssueNumber());
        response.put("number", volume.getNumber());
        response.put("description", volume.getDescription());
        response.put("published", volume.getPublished());
        response.put("archived", volume.getArchived());
        response.put("publishedAt", volume.getPublishedAt());
        response.put("journalId", volume.getResearchJournal() != null ? volume.getResearchJournal().getId() : null);
        response.put("journalKey", volume.getResearchJournal() != null ? volume.getResearchJournal().getJournalKey() : null);
        response.put("createdAt", volume.getCreatedAt());
        return response;
    }

    private String generateSlug(String title) {
        if (title == null || title.isEmpty()) {
            return null;
        }
        return title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}

