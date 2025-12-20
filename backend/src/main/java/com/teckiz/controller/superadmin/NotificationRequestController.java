package com.teckiz.controller.superadmin;

import com.teckiz.entity.Company;
import com.teckiz.entity.NotificationRequest;
import com.teckiz.repository.CompanyRepository;
import com.teckiz.repository.NotificationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/superadmin/notification-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class NotificationRequestController {

    private final NotificationRequestRepository notificationRequestRepository;
    private final CompanyRepository companyRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listNotificationRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String companyKey,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NotificationRequest> requests;

        if (companyKey != null) {
            Company company = companyRepository.findByCompanyKey(companyKey)
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            if (status != null) {
                requests = notificationRequestRepository.findByCompanyAndStatus(company, status, pageable);
            } else {
                requests = notificationRequestRepository.findByCompany(company, pageable);
            }
        } else if (status != null) {
            List<NotificationRequest> requestList = notificationRequestRepository.findAll();
            requestList = requestList.stream()
                    .filter(req -> req.getStatus().equals(status))
                    .toList();
            int start = page * size;
            int end = Math.min(start + size, requestList.size());
            List<NotificationRequest> paginated = start < requestList.size() ? requestList.subList(start, end) : List.of();
            requests = new org.springframework.data.domain.PageImpl<>(paginated, pageable, requestList.size());
        } else {
            requests = notificationRequestRepository.findAll(pageable);
        }

        if (targetType != null) {
            List<NotificationRequest> filtered = requests.getContent().stream()
                    .filter(req -> targetType.equals(req.getTargetType()))
                    .toList();
            requests = new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
        }

        List<Map<String, Object>> requestResponses = requests.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("notificationRequests", requestResponses);
        response.put("totalPages", requests.getTotalPages());
        response.put("totalElements", requests.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{requestKey}")
    public ResponseEntity<Map<String, Object>> getNotificationRequest(@PathVariable String requestKey) {
        return notificationRequestRepository.findByRequestKey(requestKey)
                .map(request -> ResponseEntity.ok(mapToResponse(request)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingRequests(
            @RequestParam(required = false) String companyKey) {

        List<NotificationRequest> pendingRequests;
        if (companyKey != null) {
            Company company = companyRepository.findByCompanyKey(companyKey)
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            pendingRequests = notificationRequestRepository.findByCompanyAndStatusAndScheduledForLessThanEqual(
                    company, "pending", LocalDateTime.now());
        } else {
            pendingRequests = notificationRequestRepository.findAll().stream()
                    .filter(req -> "pending".equals(req.getStatus()) &&
                            (req.getScheduledFor() == null || req.getScheduledFor().isBefore(LocalDateTime.now())))
                    .toList();
        }

        List<Map<String, Object>> requestResponses = pendingRequests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("notificationRequests", requestResponses, "count", pendingRequests.size()));
    }

    @PostMapping
    public ResponseEntity<?> createNotificationRequest(@RequestBody Map<String, Object> request) {
        String companyKey = (String) request.get("companyKey");
        if (companyKey == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "companyKey is required"));
        }

        Company company = companyRepository.findByCompanyKey(companyKey)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        String title = (String) request.get("title");
        String message = (String) request.get("message");
        if (title == null || message == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "title and message are required"));
        }

        NotificationRequest.NotificationRequestBuilder builder = NotificationRequest.builder()
                .company(company)
                .title(title)
                .message(message)
                .type((String) request.getOrDefault("type", "info"))
                .targetType((String) request.getOrDefault("targetType", "all"))
                .actionUrl((String) request.get("actionUrl"))
                .actionText((String) request.get("actionText"))
                .status((String) request.getOrDefault("status", "pending"))
                .metadata((String) request.get("metadata"));

        if (request.get("targetId") != null) {
            builder.targetId(((Number) request.get("targetId")).longValue());
        }
        if (request.get("targetKey") != null) {
            builder.targetKey((String) request.get("targetKey"));
        }
        if (request.get("scheduledFor") != null) {
            builder.scheduledFor(LocalDateTime.parse(request.get("scheduledFor").toString()));
        }

        NotificationRequest notificationRequest = notificationRequestRepository.save(builder.build());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Notification request created successfully", "requestKey", notificationRequest.getRequestKey()));
    }

    @PutMapping("/{requestKey}")
    public ResponseEntity<?> updateNotificationRequest(
            @PathVariable String requestKey,
            @RequestBody Map<String, Object> request) {

        NotificationRequest notificationRequest = notificationRequestRepository.findByRequestKey(requestKey)
                .orElseThrow(() -> new RuntimeException("Notification request not found"));

        if (request.get("title") != null) {
            notificationRequest.setTitle((String) request.get("title"));
        }
        if (request.get("message") != null) {
            notificationRequest.setMessage((String) request.get("message"));
        }
        if (request.get("type") != null) {
            notificationRequest.setType((String) request.get("type"));
        }
        if (request.get("targetType") != null) {
            notificationRequest.setTargetType((String) request.get("targetType"));
        }
        if (request.get("targetId") != null) {
            notificationRequest.setTargetId(((Number) request.get("targetId")).longValue());
        }
        if (request.get("targetKey") != null) {
            notificationRequest.setTargetKey((String) request.get("targetKey"));
        }
        if (request.get("actionUrl") != null) {
            notificationRequest.setActionUrl((String) request.get("actionUrl"));
        }
        if (request.get("actionText") != null) {
            notificationRequest.setActionText((String) request.get("actionText"));
        }
        if (request.get("status") != null) {
            notificationRequest.setStatus((String) request.get("status"));
            if ("completed".equals(request.get("status")) && notificationRequest.getProcessedAt() == null) {
                notificationRequest.setProcessedAt(LocalDateTime.now());
            }
        }
        if (request.get("scheduledFor") != null) {
            notificationRequest.setScheduledFor(LocalDateTime.parse(request.get("scheduledFor").toString()));
        }
        if (request.get("metadata") != null) {
            notificationRequest.setMetadata((String) request.get("metadata"));
        }

        notificationRequestRepository.save(notificationRequest);

        return ResponseEntity.ok(Map.of("message", "Notification request updated successfully"));
    }

    @DeleteMapping("/{requestKey}")
    public ResponseEntity<?> deleteNotificationRequest(@PathVariable String requestKey) {
        NotificationRequest notificationRequest = notificationRequestRepository.findByRequestKey(requestKey)
                .orElseThrow(() -> new RuntimeException("Notification request not found"));

        notificationRequestRepository.delete(notificationRequest);

        return ResponseEntity.ok(Map.of("message", "Notification request deleted successfully"));
    }

    private Map<String, Object> mapToResponse(NotificationRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", request.getId());
        response.put("requestKey", request.getRequestKey());
        response.put("title", request.getTitle());
        response.put("message", request.getMessage());
        response.put("type", request.getType());
        response.put("targetType", request.getTargetType());
        response.put("targetId", request.getTargetId());
        response.put("targetKey", request.getTargetKey());
        response.put("actionUrl", request.getActionUrl());
        response.put("actionText", request.getActionText());
        response.put("status", request.getStatus());
        response.put("processedAt", request.getProcessedAt());
        response.put("scheduledFor", request.getScheduledFor());
        response.put("metadata", request.getMetadata());
        response.put("companyId", request.getCompany().getId());
        response.put("companyName", request.getCompany().getName());
        response.put("createdAt", request.getCreatedAt());
        response.put("updatedAt", request.getUpdatedAt());
        return response;
    }
}

