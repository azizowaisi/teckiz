package com.teckiz.controller.admin.education;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.dto.PrincipalMessageRequest;
import com.teckiz.dto.PrincipalMessageResponse;
import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.PrincipalMessage;
import com.teckiz.repository.PrincipalMessageRepository;
import com.teckiz.service.ModuleAccessManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/education/admin/principal-message")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
@org.springframework.stereotype.Component("adminPrincipalMessageController")
public class PrincipalMessageController {

    private final ModuleAccessManager moduleAccessManager;
    private final PrincipalMessageRepository messageRepository;

    @GetMapping
    public ResponseEntity<PrincipalMessageResponse> getPrincipalMessage() {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        PrincipalMessage message = messageRepository
                .findByCompanyModuleMapperAndPublishedTrue(companyModuleMapper)
                .orElse(null);

        if (message == null) {
            // Try to get any message for the company
            message = messageRepository.findByCompanyAndPublishedTrue(
                    companyModuleMapper.getCompany()).orElse(null);
        }

        if (message == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(message));
    }

    @GetMapping("/{messageKey}")
    public ResponseEntity<PrincipalMessageResponse> getMessageByKey(@PathVariable String messageKey) {
        moduleAccessManager.authenticateModule();

        return messageRepository.findByMessageKey(messageKey)
                .map(message -> ResponseEntity.ok(mapToResponse(message)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createPrincipalMessage(@Valid @RequestBody PrincipalMessageRequest request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

            PrincipalMessage message = new PrincipalMessage();
        Company company = companyModuleMapper.getCompany();
        message.setCompany(company);
        message.setCompanyModuleMapper(companyModuleMapper);
        message.setTitle(request.getTitle());
        message.setMessage(request.getMessage());
        message.setPrincipalName(request.getPrincipalName());
        message.setPrincipalImage(request.getPrincipalImage());
        message.setPublished(request.getPublished() != null ? request.getPublished() : false);

        message = messageRepository.save(message);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Principal message created successfully", "messageKey", message.getMessageKey()));
    }

    @PutMapping("/{messageKey}")
    public ResponseEntity<?> updatePrincipalMessage(
            @PathVariable String messageKey,
            @Valid @RequestBody PrincipalMessageRequest request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        PrincipalMessage message = messageRepository.findByMessageKey(messageKey)
                .orElseThrow(() -> new RuntimeException("Principal message not found"));

        if (!message.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getTitle() != null) {
            message.setTitle(request.getTitle());
        }
        if (request.getMessage() != null) {
            message.setMessage(request.getMessage());
        }
        if (request.getPrincipalName() != null) {
            message.setPrincipalName(request.getPrincipalName());
        }
        if (request.getPrincipalImage() != null) {
            message.setPrincipalImage(request.getPrincipalImage());
        }
        if (request.getPublished() != null) {
            message.setPublished(request.getPublished());
        }

        message = messageRepository.save(message);

        return ResponseEntity.ok(Map.of("message", "Principal message updated successfully"));
    }

    @DeleteMapping("/{messageKey}")
    public ResponseEntity<?> deletePrincipalMessage(@PathVariable String messageKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        PrincipalMessage message = messageRepository.findByMessageKey(messageKey)
                .orElseThrow(() -> new RuntimeException("Principal message not found"));

        if (!message.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        messageRepository.delete(message);

        return ResponseEntity.ok(Map.of("message", "Principal message deleted successfully"));
    }

    private PrincipalMessageResponse mapToResponse(PrincipalMessage message) {
        return PrincipalMessageResponse.builder()
                .id(message.getId())
                .messageKey(message.getMessageKey())
                .title(message.getTitle())
                .message(message.getMessage())
                .principalName(message.getPrincipalName())
                .principalImage(message.getPrincipalImage())
                .published(message.getPublished())
                .companyId(message.getCompany().getId())
                .companyName(message.getCompany().getName())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}

