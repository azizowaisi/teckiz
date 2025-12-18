package com.teckiz.controller;

import com.teckiz.entity.Company;
import com.teckiz.entity.WebContactType;
import com.teckiz.repository.WebContactTypeRepository;
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
@RequestMapping("/website/admin/contact-types")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class WebContactTypeController {

    private final ModuleAccessManager moduleAccessManager;
    private final WebContactTypeRepository contactTypeRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listContactTypes() {
        Company company = moduleAccessManager.authenticateModule().getCompany();

        List<WebContactType> contactTypes = contactTypeRepository.findByCompany(company)
                .stream()
                .filter(type -> !Boolean.TRUE.equals(type.getArchived()))
                .sorted((a, b) -> Integer.compare(
                        a.getPosition() != null ? a.getPosition() : 0,
                        b.getPosition() != null ? b.getPosition() : 0))
                .toList();

        List<Map<String, Object>> typeResponses = contactTypes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("contactTypes", typeResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{typeKey}")
    public ResponseEntity<Map<String, Object>> getContactType(@PathVariable String typeKey) {
        moduleAccessManager.authenticateModule();

        return contactTypeRepository.findByTypeKey(typeKey)
                .map(contactType -> ResponseEntity.ok(mapToResponse(contactType)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createContactType(@RequestBody Map<String, Object> request) {
        Company company = moduleAccessManager.authenticateModule().getCompany();

        String name = (String) request.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }

        WebContactType contactType = WebContactType.builder()
                .company(company)
                .name(name)
                .position(request.get("position") != null ?
                        ((Number) request.get("position")).intValue() : 0)
                .archived(false)
                .build();

        contactType = contactTypeRepository.save(contactType);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Contact type created successfully", "typeKey", contactType.getTypeKey()));
    }

    @PutMapping("/{typeKey}")
    public ResponseEntity<?> updateContactType(
            @PathVariable String typeKey,
            @RequestBody Map<String, Object> request) {

        Company company = moduleAccessManager.authenticateModule().getCompany();

        WebContactType contactType = contactTypeRepository.findByTypeKey(typeKey)
                .orElseThrow(() -> new RuntimeException("Contact type not found"));

        if (!contactType.getCompany().getId().equals(company.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("name") != null) {
            contactType.setName((String) request.get("name"));
        }
        if (request.get("position") != null) {
            contactType.setPosition(((Number) request.get("position")).intValue());
        }
        if (request.get("archived") != null) {
            contactType.setArchived((Boolean) request.get("archived"));
        }

        contactType = contactTypeRepository.save(contactType);

        return ResponseEntity.ok(Map.of("message", "Contact type updated successfully"));
    }

    @DeleteMapping("/{typeKey}")
    public ResponseEntity<?> deleteContactType(@PathVariable String typeKey) {
        Company company = moduleAccessManager.authenticateModule().getCompany();

        WebContactType contactType = contactTypeRepository.findByTypeKey(typeKey)
                .orElseThrow(() -> new RuntimeException("Contact type not found"));

        if (!contactType.getCompany().getId().equals(company.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Soft delete
        contactType.setArchived(true);
        contactTypeRepository.save(contactType);

        return ResponseEntity.ok(Map.of("message", "Contact type deleted successfully"));
    }

    private Map<String, Object> mapToResponse(WebContactType contactType) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", contactType.getId());
        response.put("typeKey", contactType.getTypeKey());
        response.put("name", contactType.getName());
        response.put("position", contactType.getPosition());
        response.put("archived", contactType.getArchived());
        response.put("createdAt", contactType.getCreatedAt());
        return response;
    }
}

