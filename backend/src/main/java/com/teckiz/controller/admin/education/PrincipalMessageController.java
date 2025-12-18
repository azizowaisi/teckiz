package com.teckiz.controller.admin.education;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.PrincipalMessage;
import com.teckiz.repository.PrincipalMessageRepository;
import com.teckiz.service.ModuleAccessManager;
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
public class PrincipalMessageController {

    private final ModuleAccessManager moduleAccessManager;
    private final PrincipalMessageRepository messageRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPrincipalMessage() {
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
    public ResponseEntity<Map<String, Object>> getMessageByKey(@PathVariable String messageKey) {
        moduleAccessManager.authenticateModule();

        return messageRepository.findByMessageKey(messageKey)
                .map(message -> ResponseEntity.ok(mapToResponse(message)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createPrincipalMessage(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        PrincipalMessage message = PrincipalMessage.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .title((String) request.get("title"))
                .message((String) request.get("message"))
                .principalName((String) request.get("principalName"))
                .principalImage((String) request.get("principalImage"))
                .published(request.get("published") != null ?
                        (Boolean) request.get("published") : false)
                .build();

        message = messageRepository.save(message);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Principal message created successfully", "messageKey", message.getMessageKey()));
    }

    @PutMapping("/{messageKey}")
    public ResponseEntity<?> updatePrincipalMessage(
            @PathVariable String messageKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        PrincipalMessage message = messageRepository.findByMessageKey(messageKey)
                .orElseThrow(() -> new RuntimeException("Principal message not found"));

        if (!message.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("title") != null) {
            message.setTitle((String) request.get("title"));
        }
        if (request.get("message") != null) {
            message.setMessage((String) request.get("message"));
        }
        if (request.get("principalName") != null) {
            message.setPrincipalName((String) request.get("principalName"));
        }
        if (request.get("principalImage") != null) {
            message.setPrincipalImage((String) request.get("principalImage"));
        }
        if (request.get("published") != null) {
            message.setPublished((Boolean) request.get("published"));
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

    private Map<String, Object> mapToResponse(PrincipalMessage message) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", message.getId());
        response.put("messageKey", message.getMessageKey());
        response.put("title", message.getTitle());
        response.put("message", message.getMessage());
        response.put("principalName", message.getPrincipalName());
        response.put("principalImage", message.getPrincipalImage());
        response.put("published", message.getPublished());
        response.put("createdAt", message.getCreatedAt());
        response.put("updatedAt", message.getUpdatedAt());
        return response;
    }
}

