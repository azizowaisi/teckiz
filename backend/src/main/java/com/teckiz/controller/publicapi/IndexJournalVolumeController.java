package com.teckiz.controller.publicapi;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.IndexJournal;
import com.teckiz.entity.IndexJournalVolume;
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
@RequestMapping("/public/index-journals/{journalKey}/volumes")
@RequiredArgsConstructor
public class IndexJournalVolumeController {

    private final WebsiteManager websiteManager;
    private final IndexJournalRepository indexJournalRepository;
    private final IndexJournalVolumeRepository volumeRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listVolumes(
            @PathVariable String journalKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        IndexJournal journal = indexJournalRepository.findByJournalKey(journalKey)
                .orElseThrow(() -> new RuntimeException("Index journal not found"));

        if (!journal.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        if (!Boolean.TRUE.equals(journal.getActive()) || Boolean.TRUE.equals(journal.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("number").descending());
        Page<IndexJournalVolume> volumes = volumeRepository.findByCompanyAndIndexJournalAndPublishedTrueAndArchivedFalse(
                companyModuleMapper.getCompany(), journal, pageable);

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

        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        IndexJournal journal = indexJournalRepository.findByJournalKey(journalKey)
                .orElseThrow(() -> new RuntimeException("Index journal not found"));

        if (!journal.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        IndexJournalVolume volume = volumeRepository.findByVolumeKey(volumeKey)
                .orElseThrow(() -> new RuntimeException("Volume not found"));

        if (!volume.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        if (!Boolean.TRUE.equals(volume.getPublished()) || Boolean.TRUE.equals(volume.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(volume));
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
        response.put("publishedAt", volume.getPublishedAt());
        response.put("thumbnail", volume.getThumbnail());
        response.put("published", volume.getPublished());
        response.put("indexJournalId", volume.getIndexJournal() != null ? volume.getIndexJournal().getId() : null);
        response.put("indexJournalName", volume.getIndexJournal() != null ? volume.getIndexJournal().getName() : null);
        response.put("createdAt", volume.getCreatedAt());
        response.put("updatedAt", volume.getUpdatedAt());
        return response;
    }
}

