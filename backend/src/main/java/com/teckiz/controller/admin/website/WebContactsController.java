package com.teckiz.controller.admin.website;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.dto.WebContactRequest;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebContactType;
import com.teckiz.entity.WebContacts;
import com.teckiz.repository.WebContactTypeRepository;
import com.teckiz.repository.WebContactsRepository;
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
@RequestMapping("/website/admin/contacts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class WebContactsController {

    private final ModuleAccessManager moduleAccessManager;
    private final WebContactsRepository webContactsRepository;
    private final WebContactTypeRepository webContactTypeRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("position").descending());
        Page<WebContacts> contacts = webContactsRepository.findByCompanyAndArchivedFalseOrderByPositionDesc(
                companyModuleMapper.getCompany(),
                pageable
        );

        List<Map<String, Object>> contactResponses = contacts.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("contacts", contactResponses);
        response.put("totalPages", contacts.getTotalPages());
        response.put("totalElements", contacts.getTotalElements());
        response.put("currentPage", page);
        response.put("leftTab", "web-contacts");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{contactKey}")
    public ResponseEntity<Map<String, Object>> getContact(@PathVariable String contactKey) {
        moduleAccessManager.authenticateModule();
        
        return webContactsRepository.findByContactKey(contactKey)
                .map(contact -> ResponseEntity.ok(mapToResponse(contact)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createContact(@RequestBody WebContactRequest request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        if (request.getName() == null || request.getName().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }

        WebContacts contact = WebContacts.builder()
                .company(companyModuleMapper.getCompany())
                .name(request.getName())
                .role(request.getRole())
                .description(request.getDescription())
                .url(request.getUrl())
                .email(request.getEmail())
                .phone(request.getPhone())
                .twitter(request.getTwitter())
                .facebook(request.getFacebook())
                .instagram(request.getInstagram())
                .linkedin(request.getLinkedin())
                .researchGate(request.getResearchGate())
                .position(request.getPosition() != null ? request.getPosition() : 0)
                .thumbnail(request.getThumbnail())
                .build();

        // Add contact types if provided
        if (request.getContactTypeKeys() != null && !request.getContactTypeKeys().isEmpty()) {
            List<WebContactType> contactTypes = request.getContactTypeKeys().stream()
                    .map(key -> webContactTypeRepository.findByTypeKey(key))
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toList());
            contact.setContactTypes(contactTypes);
        }

        contact = webContactsRepository.save(contact);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Contact created successfully", "contactKey", contact.getContactKey()));
    }

    @PutMapping("/{contactKey}")
    public ResponseEntity<?> updateContact(
            @PathVariable String contactKey,
            @RequestBody WebContactRequest request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebContacts contact = webContactsRepository.findByContactKey(contactKey)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        if (!contact.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        contact.setName(request.getName());
        contact.setRole(request.getRole());
        contact.setDescription(request.getDescription());
        contact.setUrl(request.getUrl());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setTwitter(request.getTwitter());
        contact.setFacebook(request.getFacebook());
        contact.setInstagram(request.getInstagram());
        contact.setLinkedin(request.getLinkedin());
        contact.setResearchGate(request.getResearchGate());
        if (request.getPosition() != null) {
            contact.setPosition(request.getPosition());
        }
        if (request.getThumbnail() != null) {
            contact.setThumbnail(request.getThumbnail());
        }

        // Update contact types
        if (request.getContactTypeKeys() != null) {
            List<WebContactType> contactTypes = request.getContactTypeKeys().stream()
                    .map(key -> webContactTypeRepository.findByTypeKey(key))
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toList());
            contact.setContactTypes(contactTypes);
        }

        contact = webContactsRepository.save(contact);

        return ResponseEntity.ok(Map.of("message", "Contact updated successfully"));
    }

    @DeleteMapping("/{contactKey}")
    public ResponseEntity<?> deleteContact(@PathVariable String contactKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebContacts contact = webContactsRepository.findByContactKey(contactKey)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        if (!contact.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        contact.setArchived(true);
        webContactsRepository.save(contact);

        return ResponseEntity.ok(Map.of("message", "Contact archived successfully"));
    }

    private Map<String, Object> mapToResponse(WebContacts contact) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", contact.getId());
        response.put("contactKey", contact.getContactKey());
        response.put("name", contact.getName());
        response.put("role", contact.getRole());
        response.put("description", contact.getDescription());
        response.put("url", contact.getUrl());
        response.put("email", contact.getEmail());
        response.put("phone", contact.getPhone());
        response.put("twitter", contact.getTwitter());
        response.put("facebook", contact.getFacebook());
        response.put("instagram", contact.getInstagram());
        response.put("linkedin", contact.getLinkedin());
        response.put("researchGate", contact.getResearchGate());
        response.put("position", contact.getPosition());
        response.put("thumbnail", contact.getThumbnail());
        response.put("archived", contact.getArchived());
        if (contact.getContactTypes() != null) {
            response.put("contactTypes", contact.getContactTypes().stream()
                    .map(type -> Map.of("id", type.getId(), "name", type.getName()))
                    .collect(Collectors.toList()));
        }
        return response;
    }
}

