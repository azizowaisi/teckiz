package com.teckiz.controller.superadmin;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.teckiz.entity.Company;
import com.teckiz.entity.EmailTemplate;
import com.teckiz.repository.CompanyRepository;
import com.teckiz.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/superadmin/email-templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class EmailTemplateController {

    private final EmailTemplateRepository emailTemplateRepository;
    private final CompanyRepository companyRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listEmailTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Boolean active) {

        List<EmailTemplate> templates;

        if (companyId != null) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            if (active != null && active) {
                templates = emailTemplateRepository.findByCompanyAndActiveTrue(company);
            } else {
                templates = emailTemplateRepository.findByCompany(company);
            }
        } else {
            if (active != null && active) {
                templates = emailTemplateRepository.findByActiveTrue();
            } else {
                templates = emailTemplateRepository.findAll();
            }
        }

        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, templates.size());
        List<EmailTemplate> paginated = start < templates.size() ? 
                templates.subList(start, end) : List.of();

        List<Map<String, Object>> templateResponses = paginated.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("templates", templateResponses);
        response.put("totalPages", (int) Math.ceil((double) templates.size() / size));
        response.put("totalElements", templates.size());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{templateKey}")
    public ResponseEntity<Map<String, Object>> getEmailTemplate(@PathVariable String templateKey) {
        return emailTemplateRepository.findByTemplateKey(templateKey)
                .map(template -> ResponseEntity.ok(mapToResponse(template)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{templateType}")
    public ResponseEntity<Map<String, Object>> getEmailTemplateByType(
            @PathVariable String templateType,
            @RequestParam(required = false) Long companyId) {

        EmailTemplate template;
        if (companyId != null) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            template = emailTemplateRepository.findByTemplateTypeAndCompany(templateType, company)
                    .orElse(null);
            if (template == null) {
                template = emailTemplateRepository.findByTemplateType(templateType).orElse(null);
            }
        } else {
            template = emailTemplateRepository.findByTemplateType(templateType).orElse(null);
        }

        if (template == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(template));
    }

    @PostMapping
    public ResponseEntity<?> createEmailTemplate(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String subject = (String) request.get("subject");
        String htmlBody = (String) request.get("htmlBody");
        String templateType = (String) request.get("templateType");

        if (name == null || subject == null || htmlBody == null || templateType == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name, subject, htmlBody, and templateType are required"));
        }

        EmailTemplate.EmailTemplateBuilder builder = EmailTemplate.builder()
                .name(name)
                .subject(subject)
                .htmlBody(htmlBody)
                .textBody((String) request.get("textBody"))
                .templateType(templateType)
                .variables((String) request.get("variables"))
                .active(request.get("active") != null ? (Boolean) request.get("active") : true);

        if (request.get("companyId") != null) {
            Long companyId = ((Number) request.get("companyId")).longValue();
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            builder.company(company);
        }

        EmailTemplate template = emailTemplateRepository.save(builder.build());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Email template created successfully", "templateKey", template.getTemplateKey()));
    }

    @PutMapping("/{templateKey}")
    public ResponseEntity<?> updateEmailTemplate(
            @PathVariable String templateKey,
            @RequestBody Map<String, Object> request) {

        EmailTemplate template = emailTemplateRepository.findByTemplateKey(templateKey)
                .orElseThrow(() -> new RuntimeException("Email template not found"));

        if (request.get("name") != null) {
            template.setName((String) request.get("name"));
        }
        if (request.get("subject") != null) {
            template.setSubject((String) request.get("subject"));
        }
        if (request.get("htmlBody") != null) {
            template.setHtmlBody((String) request.get("htmlBody"));
        }
        if (request.get("textBody") != null) {
            template.setTextBody((String) request.get("textBody"));
        }
        if (request.get("templateType") != null) {
            template.setTemplateType((String) request.get("templateType"));
        }
        if (request.get("variables") != null) {
            template.setVariables((String) request.get("variables"));
        }
        if (request.get("active") != null) {
            template.setActive((Boolean) request.get("active"));
        }

        emailTemplateRepository.save(template);

        return ResponseEntity.ok(Map.of("message", "Email template updated successfully"));
    }

    @DeleteMapping("/{templateKey}")
    public ResponseEntity<?> deleteEmailTemplate(@PathVariable String templateKey) {
        EmailTemplate template = emailTemplateRepository.findByTemplateKey(templateKey)
                .orElseThrow(() -> new RuntimeException("Email template not found"));

        emailTemplateRepository.delete(template);

        return ResponseEntity.ok(Map.of("message", "Email template deleted successfully"));
    }

    private Map<String, Object> mapToResponse(EmailTemplate template) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", template.getId());
        response.put("templateKey", template.getTemplateKey());
        response.put("name", template.getName());
        response.put("subject", template.getSubject());
        response.put("htmlBody", template.getHtmlBody());
        response.put("textBody", template.getTextBody());
        response.put("templateType", template.getTemplateType());
        response.put("variables", template.getVariables());
        response.put("active", template.getActive());
        response.put("companyId", template.getCompany() != null ? template.getCompany().getId() : null);
        response.put("companyName", template.getCompany() != null ? template.getCompany().getName() : null);
        response.put("createdAt", template.getCreatedAt());
        response.put("updatedAt", template.getUpdatedAt());
        return response;
    }
}

