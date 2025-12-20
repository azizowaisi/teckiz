package com.teckiz.controller.publicapi;

import com.teckiz.dto.FacilityResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Facility;
import com.teckiz.repository.FacilityRepository;
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
@RequestMapping("/public/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final WebsiteManager websiteManager;
    private final FacilityRepository facilityRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listFacilities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Facility> facilities = facilityRepository.findByCompanyAndPublishedTrueAndArchivedFalse(
                companyModuleMapper.getCompany(), pageable);

        List<FacilityResponse> responses = facilities.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("facilities", responses);
        response.put("totalPages", facilities.getTotalPages());
        response.put("totalElements", facilities.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{facilityKey}")
    public ResponseEntity<FacilityResponse> getFacility(@PathVariable String facilityKey) {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        Facility facility = facilityRepository.findByFacilityKey(facilityKey)
                .orElseThrow(() -> new RuntimeException("Facility not found"));

        if (!facility.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.notFound().build();
        }

        if (!Boolean.TRUE.equals(facility.getPublished()) || Boolean.TRUE.equals(facility.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(facility));
    }

    private FacilityResponse mapToResponse(Facility facility) {
        return FacilityResponse.builder()
                .id(facility.getId())
                .facilityKey(facility.getFacilityKey())
                .name(facility.getName())
                .description(facility.getDescription())
                .thumbnail(facility.getThumbnail())
                .published(facility.getPublished())
                .archived(facility.getArchived())
                .companyId(facility.getCompany().getId())
                .companyName(facility.getCompany().getName())
                .createdAt(facility.getCreatedAt())
                .updatedAt(facility.getUpdatedAt())
                .build();
    }
}

