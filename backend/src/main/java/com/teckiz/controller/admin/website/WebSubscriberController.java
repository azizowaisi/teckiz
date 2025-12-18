package com.teckiz.controller.admin.website;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebSubscriber;
import com.teckiz.repository.WebSubscriberRepository;
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
@RequestMapping("/website/admin/subscribers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class WebSubscriberController {

    private final ModuleAccessManager moduleAccessManager;
    private final WebSubscriberRepository subscriberRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listSubscribers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean active) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        if (active != null) {
            if (active) {
                List<WebSubscriber> activeList = subscriberRepository.findByCompanyAndActiveTrue(
                        companyModuleMapper.getCompany());
                int start = page * size;
                int end = Math.min(start + size, activeList.size());
                List<WebSubscriber> paginated = activeList.subList(start, end);
                
                Map<String, Object> response = new HashMap<>();
                response.put("subscribers", paginated.stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()));
                response.put("totalPages", (int) Math.ceil((double) activeList.size() / size));
                response.put("totalElements", activeList.size());
                response.put("currentPage", page);
                return ResponseEntity.ok(response);
            }
        }

        List<WebSubscriber> allSubscribers = subscriberRepository.findByCompany(companyModuleMapper.getCompany());
        int start = page * size;
        int end = Math.min(start + size, allSubscribers.size());
        List<WebSubscriber> paginated = allSubscribers.subList(start, end);

        Map<String, Object> response = new HashMap<>();
        response.put("subscribers", paginated.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
        response.put("totalPages", (int) Math.ceil((double) allSubscribers.size() / size));
        response.put("totalElements", allSubscribers.size());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{subscriberKey}")
    public ResponseEntity<Map<String, Object>> getSubscriber(@PathVariable String subscriberKey) {
        moduleAccessManager.authenticateModule();

        return subscriberRepository.findBySubscriberKey(subscriberKey)
                .map(subscriber -> ResponseEntity.ok(mapToResponse(subscriber)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createSubscriber(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String email = (String) request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email is required"));
        }

        // Check if subscriber already exists
        if (subscriberRepository.findByCompanyAndEmail(companyModuleMapper.getCompany(), email).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Subscriber with this email already exists"));
        }

        WebSubscriber subscriber = WebSubscriber.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .email(email)
                .name((String) request.get("name"))
                .active(request.get("active") != null ?
                        (Boolean) request.get("active") : true)
                .verified(request.get("verified") != null ?
                        (Boolean) request.get("verified") : false)
                .build();

        subscriber = subscriberRepository.save(subscriber);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Subscriber created successfully", "subscriberKey", subscriber.getSubscriberKey()));
    }

    @PutMapping("/{subscriberKey}")
    public ResponseEntity<?> updateSubscriber(
            @PathVariable String subscriberKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebSubscriber subscriber = subscriberRepository.findBySubscriberKey(subscriberKey)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));

        if (!subscriber.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("email") != null) {
            String newEmail = (String) request.get("email");
            final Long currentSubscriberId = subscriber.getId();
            // Check if email is already taken by another subscriber
            subscriberRepository.findByCompanyAndEmail(companyModuleMapper.getCompany(), newEmail)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(currentSubscriberId)) {
                            throw new RuntimeException("Email already taken by another subscriber");
                        }
                    });
            subscriber.setEmail(newEmail);
        }
        if (request.get("name") != null) {
            subscriber.setName((String) request.get("name"));
        }
        if (request.get("active") != null) {
            subscriber.setActive((Boolean) request.get("active"));
        }
        if (request.get("verified") != null) {
            subscriber.setVerified((Boolean) request.get("verified"));
        }

        subscriber = subscriberRepository.save(subscriber);

        return ResponseEntity.ok(Map.of("message", "Subscriber updated successfully"));
    }

    @DeleteMapping("/{subscriberKey}")
    public ResponseEntity<?> deleteSubscriber(@PathVariable String subscriberKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebSubscriber subscriber = subscriberRepository.findBySubscriberKey(subscriberKey)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));

        if (!subscriber.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        subscriberRepository.delete(subscriber);

        return ResponseEntity.ok(Map.of("message", "Subscriber deleted successfully"));
    }

    @PostMapping("/{subscriberKey}/verify")
    public ResponseEntity<?> verifySubscriber(@PathVariable String subscriberKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebSubscriber subscriber = subscriberRepository.findBySubscriberKey(subscriberKey)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));

        if (!subscriber.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        subscriber.setVerified(true);
        subscriber = subscriberRepository.save(subscriber);

        return ResponseEntity.ok(Map.of("message", "Subscriber verified successfully"));
    }

    private Map<String, Object> mapToResponse(WebSubscriber subscriber) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", subscriber.getId());
        response.put("subscriberKey", subscriber.getSubscriberKey());
        response.put("email", subscriber.getEmail());
        response.put("name", subscriber.getName());
        response.put("active", subscriber.getActive());
        response.put("verified", subscriber.getVerified());
        response.put("createdAt", subscriber.getCreatedAt());
        return response;
    }
}

