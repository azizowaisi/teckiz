package com.teckiz.controller.admin.education;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Facility;
import com.teckiz.repository.FacilityRepository;
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

        List<Map<String, Object>> facilityResponses = facilities.getContent().stream()
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
    public ResponseEntity<Map<String, Object>> getFacility(@PathVariable String facilityKey) {
        moduleAccessManager.authenticateModule();

        return facilityRepository.findByFacilityKey(facilityKey)
                .map(facility -> ResponseEntity.ok(mapToResponse(facility)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createFacility(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String name = (String) request.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }

        Facility facility = Facility.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .name(name)
                .description((String) request.get("description"))
                .thumbnail((String) request.get("thumbnail"))
                .published(request.get("published") != null ?
                        (Boolean) request.get("published") : false)
                .archived(false)
                .build();

        facility = facilityRepository.save(facility);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Facility created successfully", "facilityKey", facility.getFacilityKey()));
    }

    @PutMapping("/{facilityKey}")
    public ResponseEntity<?> updateFacility(
            @PathVariable String facilityKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Facility facility = facilityRepository.findByFacilityKey(facilityKey)
                .orElseThrow(() -> new RuntimeException("Facility not found"));

        if (!facility.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("name") != null) {
            facility.setName((String) request.get("name"));
        }
        if (request.get("description") != null) {
            facility.setDescription((String) request.get("description"));
        }
        if (request.get("thumbnail") != null) {
            facility.setThumbnail((String) request.get("thumbnail"));
        }
        if (request.get("published") != null) {
            facility.setPublished((Boolean) request.get("published"));
        }
        if (request.get("archived") != null) {
            facility.setArchived((Boolean) request.get("archived"));
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

    private Map<String, Object> mapToResponse(Facility facility) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", facility.getId());
        response.put("facilityKey", facility.getFacilityKey());
        response.put("name", facility.getName());
        response.put("description", facility.getDescription());
        response.put("thumbnail", facility.getThumbnail());
        response.put("published", facility.getPublished());
        response.put("archived", facility.getArchived());
        response.put("createdAt", facility.getCreatedAt());
        response.put("updatedAt", facility.getUpdatedAt());
        return response;
    }
}

