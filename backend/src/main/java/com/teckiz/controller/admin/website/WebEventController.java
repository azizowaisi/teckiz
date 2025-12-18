package com.teckiz.controller.admin.website;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebContacts;
import com.teckiz.entity.WebEvent;
import com.teckiz.repository.WebContactsRepository;
import com.teckiz.repository.WebEventRepository;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/website/admin/events")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class WebEventController {

    private final ModuleAccessManager moduleAccessManager;
    private final WebEventRepository webEventRepository;
    private final WebContactsRepository webContactsRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchKey,
            @RequestParam(required = false) String filter) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());
        Page<WebEvent> events;

        if ("published".equals(filter)) {
            events = webEventRepository.findByCompanyAndPublishedTrueAndArchivedFalse(
                    companyModuleMapper.getCompany(),
                    pageable
            );
        } else {
            events = webEventRepository.findByCompany(companyModuleMapper.getCompany(), pageable);
        }

        List<Map<String, Object>> eventResponses = events.getContent().stream()
                .map(event -> {
                    Map<String, Object> eventMap = new HashMap<>();
                    eventMap.put("id", event.getId());
                    eventMap.put("eventKey", event.getEventKey());
                    eventMap.put("title", event.getTitle());
                    eventMap.put("slug", event.getSlug());
                    eventMap.put("description", event.getDescription());
                    eventMap.put("published", event.getPublished());
                    eventMap.put("archived", event.getArchived());
                    eventMap.put("startDate", event.getStartDate());
                    eventMap.put("endDate", event.getEndDate());
                    eventMap.put("location", event.getLocation());
                    eventMap.put("coordinates", event.getCoordinates());
                    eventMap.put("carousel", event.getCarousel());
                    eventMap.put("createdAt", event.getCreatedAt());
                    return eventMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("events", eventResponses);
        response.put("totalPages", events.getTotalPages());
        response.put("totalElements", events.getTotalElements());
        response.put("currentPage", page);
        response.put("leftTab", "web-events");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String title = (String) request.get("title");
        if (title == null || title.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Title is required"));
        }

        WebEvent event = WebEvent.builder()
                .company(companyModuleMapper.getCompany())
                .title(title)
                .slug(generateSlug(title))
                .description((String) request.get("description"))
                .published(request.get("published") != null ? (Boolean) request.get("published") : false)
                .publishedAt(request.get("published") != null && (Boolean) request.get("published") ? 
                        LocalDateTime.now() : null)
                .startDate(parseDateTime(request.get("startDate")))
                .endDate(parseDateTime(request.get("endDate")))
                .location((String) request.get("location"))
                .coordinates((String) request.get("coordinates"))
                .embedCode((String) request.get("embedCode"))
                .carousel(request.get("carousel") != null ? (Boolean) request.get("carousel") : false)
                .build();

        // Add contacts if provided
        if (request.get("contactKeys") != null) {
            @SuppressWarnings("unchecked")
            List<String> contactKeys = (List<String>) request.get("contactKeys");
            List<WebContacts> contacts = contactKeys.stream()
                    .map(key -> webContactsRepository.findByContactKey(key))
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toList());
            event.setContacts(contacts);
        }

        event = webEventRepository.save(event);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Event created successfully", "eventKey", event.getEventKey()));
    }

    @PutMapping("/{eventKey}")
    public ResponseEntity<?> updateEvent(
            @PathVariable String eventKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebEvent event = webEventRepository.findByEventKey(eventKey)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("title") != null) {
            event.setTitle((String) request.get("title"));
            event.setSlug(generateSlug((String) request.get("title")));
        }
        if (request.get("description") != null) {
            event.setDescription((String) request.get("description"));
        }
        if (request.get("published") != null) {
            event.setPublished((Boolean) request.get("published"));
            if ((Boolean) request.get("published") && event.getPublishedAt() == null) {
                event.setPublishedAt(LocalDateTime.now());
            }
        }
        if (request.get("startDate") != null) {
            event.setStartDate(parseDateTime(request.get("startDate")));
        }
        if (request.get("endDate") != null) {
            event.setEndDate(parseDateTime(request.get("endDate")));
        }
        if (request.get("location") != null) {
            event.setLocation((String) request.get("location"));
        }
        if (request.get("coordinates") != null) {
            event.setCoordinates((String) request.get("coordinates"));
        }
        if (request.get("embedCode") != null) {
            event.setEmbedCode((String) request.get("embedCode"));
        }
        if (request.get("carousel") != null) {
            event.setCarousel((Boolean) request.get("carousel"));
        }

        event = webEventRepository.save(event);

        return ResponseEntity.ok(Map.of("message", "Event updated successfully"));
    }

    @DeleteMapping("/{eventKey}")
    public ResponseEntity<?> deleteEvent(@PathVariable String eventKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebEvent event = webEventRepository.findByEventKey(eventKey)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        event.setArchived(true);
        webEventRepository.save(event);

        return ResponseEntity.ok(Map.of("message", "Event archived successfully"));
    }

    private String generateSlug(String title) {
        if (title == null) {
            return null;
        }
        return title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    private LocalDateTime parseDateTime(Object dateTime) {
        if (dateTime == null) {
            return null;
        }
        if (dateTime instanceof String) {
            try {
                return LocalDateTime.parse((String) dateTime);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}

