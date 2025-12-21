package com.teckiz.controller.admin;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Statistics;
import com.teckiz.repository.StatisticsRepository;
import com.teckiz.service.ModuleAccessManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
@Tag(name = "Admin - Statistics", description = "Admin endpoints for viewing statistics and analytics")
public class StatisticsController {

    private final ModuleAccessManager moduleAccessManager;
    private final StatisticsRepository statisticsRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listStatistics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String statType,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();
        Company company = companyModuleMapper.getCompany();

        Pageable pageable = PageRequest.of(page, size, Sort.by("recordedAt").descending());
        Page<Statistics> statistics;

        if (statType != null && startDate != null && endDate != null) {
            List<Statistics> statsList = statisticsRepository.findByCompanyAndStatTypeAndRecordedAtBetween(
                    company, statType, startDate, endDate);
            int start = page * size;
            int end = Math.min(start + size, statsList.size());
            List<Statistics> paginated = start < statsList.size() ? statsList.subList(start, end) : List.of();
            statistics = new org.springframework.data.domain.PageImpl<>(paginated, pageable, statsList.size());
        } else if (startDate != null && endDate != null) {
            List<Statistics> statsList = statisticsRepository.findByCompanyAndRecordedAtBetween(company, startDate, endDate);
            int start = page * size;
            int end = Math.min(start + size, statsList.size());
            List<Statistics> paginated = start < statsList.size() ? statsList.subList(start, end) : List.of();
            statistics = new org.springframework.data.domain.PageImpl<>(paginated, pageable, statsList.size());
        } else if (statType != null) {
            List<Statistics> statsList = statisticsRepository.findByCompanyAndStatType(company, statType);
            int start = page * size;
            int end = Math.min(start + size, statsList.size());
            List<Statistics> paginated = start < statsList.size() ? statsList.subList(start, end) : List.of();
            statistics = new org.springframework.data.domain.PageImpl<>(paginated, pageable, statsList.size());
        } else {
            statistics = statisticsRepository.findByCompany(company, pageable);
        }

        List<Map<String, Object>> statResponses = statistics.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("statistics", statResponses);
        response.put("totalPages", statistics.getTotalPages());
        response.put("totalElements", statistics.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getStatisticsSummary(
            @RequestParam(required = false) String statType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();
        Company company = companyModuleMapper.getCompany();

        Map<String, Object> summary = new HashMap<>();

        if (statType != null && startDate != null && endDate != null) {
            Long count = statisticsRepository.countByCompanyAndStatTypeAndRecordedAtBetween(
                    company, statType, startDate, endDate);
            summary.put("totalCount", count);
            summary.put("statType", statType);
            summary.put("startDate", startDate);
            summary.put("endDate", endDate);
        } else if (statType != null) {
            Long count = statisticsRepository.countByCompanyAndStatType(company, statType);
            summary.put("totalCount", count);
            summary.put("statType", statType);
        } else {
            summary.put("message", "Please provide statType or date range");
        }

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/top-entities")
    public ResponseEntity<Map<String, Object>> getTopEntities(
            @RequestParam String statType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "10") int limit) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();
        Company company = companyModuleMapper.getCompany();

        List<Object[]> topEntities = statisticsRepository.findTopEntitiesByStatType(
                company, statType, startDate, endDate);

        List<Map<String, Object>> results = topEntities.stream()
                .limit(limit)
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("entityType", result[0]);
                    item.put("entityId", result[1]);
                    item.put("count", result[2]);
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("topEntities", results);
        response.put("statType", statType);
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createStatistics(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String statType = (String) request.get("statType");
        if (statType == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "statType is required"));
        }

        Statistics.StatisticsBuilder builder = Statistics.builder()
                .statType(statType)
                .entityType((String) request.get("entityType"))
                .entityKey((String) request.get("entityKey"))
                .ipAddress((String) request.get("ipAddress"))
                .userAgent((String) request.get("userAgent"))
                .referrer((String) request.get("referrer"))
                .sessionId((String) request.get("sessionId"))
                .metadata((String) request.get("metadata"))
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper);

        if (request.get("entityId") != null) {
            builder.entityId(((Number) request.get("entityId")).longValue());
        }
        if (request.get("userId") != null) {
            builder.userId(((Number) request.get("userId")).longValue());
        }

        Statistics statistics = statisticsRepository.save(builder.build());

        return ResponseEntity.ok(Map.of("message", "Statistics recorded", "statKey", statistics.getStatKey()));
    }

    private Map<String, Object> mapToResponse(Statistics statistics) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", statistics.getId());
        response.put("statKey", statistics.getStatKey());
        response.put("statType", statistics.getStatType());
        response.put("entityType", statistics.getEntityType());
        response.put("entityId", statistics.getEntityId());
        response.put("entityKey", statistics.getEntityKey());
        response.put("userId", statistics.getUserId());
        response.put("ipAddress", statistics.getIpAddress());
        response.put("userAgent", statistics.getUserAgent());
        response.put("referrer", statistics.getReferrer());
        response.put("sessionId", statistics.getSessionId());
        response.put("metadata", statistics.getMetadata());
        response.put("recordedAt", statistics.getRecordedAt());
        return response;
    }
}

