package com.teckiz.controller.admin.journal;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.IndexJournal;
import com.teckiz.entity.IndexJournalVolume;
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
@RequestMapping("/journal/admin/index-journals/{journalKey}/volumes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class IndexJournalVolumeController {

    private final ModuleAccessManager moduleAccessManager;
    private final IndexJournalRepository indexJournalRepository;
    private final IndexJournalVolumeRepository volumeRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listVolumes(
            @PathVariable String journalKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean published) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        IndexJournal journal = indexJournalRepository.findByJournalKey(journalKey)
                .orElseThrow(() -> new RuntimeException("Index journal not found"));

        if (!journal.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("number").descending());
        Page<IndexJournalVolume> volumes;

        if (published != null && published) {
            volumes = volumeRepository.findByCompanyAndIndexJournalAndPublishedTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(), journal, pageable);
        } else {
            volumes = volumeRepository.findByCompanyAndIndexJournalAndArchivedFalse(
                    companyModuleMapper.getCompany(), journal, pageable);
        }

        List<Map<String, Object>> volumeResponses = volumes.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("volumes", volumeResponses);
        response.put("totalPages", volumes.getTotalPages());
        response.put("totalElements", volumes.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{volumeKey}")
    public ResponseEntity<Map<String, Object>> getVolume(
            @PathVariable String journalKey,
            @PathVariable String volumeKey) {

        moduleAccessManager.authenticateModule();

        return volumeRepository.findByVolumeKey(volumeKey)
                .map(volume -> ResponseEntity.ok(mapToResponse(volume)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createVolume(
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

        IndexJournalVolume volume = IndexJournalVolume.builder()
                .company(companyModuleMapper.getCompany())
                .indexJournal(journal)
                .title(title)
                .subTitle((String) request.get("subTitle"))
                .number(request.get("number") != null ? ((Number) request.get("number")).intValue() : null)
                .volumeNumber(request.get("volumeNumber") != null ? ((Number) request.get("volumeNumber")).intValue() : null)
                .issueNumber(request.get("issueNumber") != null ? ((Number) request.get("issueNumber")).intValue() : null)
                .slug(slug)
                .description((String) request.get("description"))
                .thumbnail((String) request.get("thumbnail"))
                .published(request.get("published") != null ? (Boolean) request.get("published") : false)
                .archived(false)
                .build();

        volume = volumeRepository.save(volume);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Volume created successfully", "volumeKey", volume.getVolumeKey()));
    }

    @PutMapping("/{volumeKey}")
    public ResponseEntity<?> updateVolume(
            @PathVariable String journalKey,
            @PathVariable String volumeKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        IndexJournalVolume volume = volumeRepository.findByVolumeKey(volumeKey)
                .orElseThrow(() -> new RuntimeException("Volume not found"));

        if (!volume.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("title") != null) {
            volume.setTitle((String) request.get("title"));
        }
        if (request.get("subTitle") != null) {
            volume.setSubTitle((String) request.get("subTitle"));
        }
        if (request.get("number") != null) {
            volume.setNumber(((Number) request.get("number")).intValue());
        }
        if (request.get("volumeNumber") != null) {
            volume.setVolumeNumber(((Number) request.get("volumeNumber")).intValue());
        }
        if (request.get("issueNumber") != null) {
            volume.setIssueNumber(((Number) request.get("issueNumber")).intValue());
        }
        if (request.get("slug") != null) {
            volume.setSlug((String) request.get("slug"));
        }
        if (request.get("description") != null) {
            volume.setDescription((String) request.get("description"));
        }
        if (request.get("thumbnail") != null) {
            volume.setThumbnail((String) request.get("thumbnail"));
        }
        if (request.get("published") != null) {
            volume.setPublished((Boolean) request.get("published"));
        }
        if (request.get("archived") != null) {
            volume.setArchived((Boolean) request.get("archived"));
        }

        volume = volumeRepository.save(volume);

        return ResponseEntity.ok(Map.of("message", "Volume updated successfully"));
    }

    @DeleteMapping("/{volumeKey}")
    public ResponseEntity<?> deleteVolume(
            @PathVariable String journalKey,
            @PathVariable String volumeKey) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        IndexJournalVolume volume = volumeRepository.findByVolumeKey(volumeKey)
                .orElseThrow(() -> new RuntimeException("Volume not found"));

        if (!volume.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        volume.setArchived(true);
        volumeRepository.save(volume);

        return ResponseEntity.ok(Map.of("message", "Volume deleted successfully"));
    }

    private Map<String, Object> mapToResponse(IndexJournalVolume volume) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", volume.getId());
        response.put("volumeKey", volume.getVolumeKey());
        response.put("title", volume.getTitle());
        response.put("subTitle", volume.getSubTitle());
        response.put("number", volume.getNumber());
        response.put("volumeNumber", volume.getVolumeNumber());
        response.put("issueNumber", volume.getIssueNumber());
        response.put("slug", volume.getSlug());
        response.put("description", volume.getDescription());
        response.put("thumbnail", volume.getThumbnail());
        response.put("published", volume.getPublished());
        response.put("publishedAt", volume.getPublishedAt());
        response.put("archived", volume.getArchived());
        response.put("createdAt", volume.getCreatedAt());
        response.put("updatedAt", volume.getUpdatedAt());
        return response;
    }
}

