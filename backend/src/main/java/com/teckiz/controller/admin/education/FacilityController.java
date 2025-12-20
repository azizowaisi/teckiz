package com.teckiz.controller.admin.education;

import com.teckiz.dto.FacilityRequest;
import com.teckiz.dto.FacilityResponse;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Facility;
import com.teckiz.repository.FacilityRepository;
import com.teckiz.service.ModuleAccessManager;
import jakarta.validation.Valid;
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
@RequestMapping("/education/admin/facilities")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class FacilityController {

    private final ModuleAccessManager moduleAccessManager;
    private final FacilityRepository facilityRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listFacilities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean published) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Facility> facilities;

        if (published != null && published) {
            facilities = facilityRepository.findByCompanyAndPublishedTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(), pageable);
        } else {
            facilities = facilityRepository.findByCompany(companyModuleMapper.getCompany(), pageable);
        }

        List<FacilityResponse> facilityResponses = facilities.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("facilities", facilityResponses);
        response.put("totalPages", facilities.getTotalPages());
        response.put("totalElements", facilities.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{facilityKey}")
    public ResponseEntity<FacilityResponse> getFacility(@PathVariable String facilityKey) {
        moduleAccessManager.authenticateModule();

        return facilityRepository.findByFacilityKey(facilityKey)
                .map(facility -> ResponseEntity.ok(mapToResponse(facility)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createFacility(@Valid @RequestBody FacilityRequest request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Facility facility = Facility.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .name(request.getName())
                .description(request.getDescription())
                .thumbnail(request.getThumbnail())
                .published(request.getPublished() != null ? request.getPublished() : false)
                .archived(false)
                .build();

        facility = facilityRepository.save(facility);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Facility created successfully", "facilityKey", facility.getFacilityKey()));
    }

    @PutMapping("/{facilityKey}")
    public ResponseEntity<?> updateFacility(
            @PathVariable String facilityKey,
            @Valid @RequestBody FacilityRequest request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Facility facility = facilityRepository.findByFacilityKey(facilityKey)
                .orElseThrow(() -> new RuntimeException("Facility not found"));

        if (!facility.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getName() != null) {
            facility.setName(request.getName());
        }
        if (request.getDescription() != null) {
            facility.setDescription(request.getDescription());
        }
        if (request.getThumbnail() != null) {
            facility.setThumbnail(request.getThumbnail());
        }
        if (request.getPublished() != null) {
            facility.setPublished(request.getPublished());
        }

        facility = facilityRepository.save(facility);

        return ResponseEntity.ok(Map.of("message", "Facility updated successfully"));
    }

    @DeleteMapping("/{facilityKey}")
    public ResponseEntity<?> deleteFacility(@PathVariable String facilityKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Facility facility = facilityRepository.findByFacilityKey(facilityKey)
                .orElseThrow(() -> new RuntimeException("Facility not found"));

        if (!facility.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        facility.setArchived(true);
        facilityRepository.save(facility);

        return ResponseEntity.ok(Map.of("message", "Facility deleted successfully"));
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

