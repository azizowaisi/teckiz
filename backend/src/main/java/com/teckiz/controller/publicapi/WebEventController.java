package com.teckiz.controller.publicapi;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebEvent;
import com.teckiz.repository.WebEventRepository;
import com.teckiz.service.WebsiteManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public/events")
@RequiredArgsConstructor
public class WebEventController {

    private final WebsiteManager websiteManager;
    private final WebEventRepository webEventRepository;

    @GetMapping("/upcoming")
    public ResponseEntity<List<Map<String, Object>>> getUpcomingEvents() {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        List<WebEvent> upcomingEvents = webEventRepository.findUpcomingEvents(
                companyModuleMapper.getCompany(),
                LocalDateTime.now()
        );

        List<Map<String, Object>> eventResponses = upcomingEvents.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(eventResponses);
    }

    @GetMapping("/past")
    public ResponseEntity<List<Map<String, Object>>> getPastEvents() {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        List<WebEvent> pastEvents = webEventRepository.findPastEvents(
                companyModuleMapper.getCompany(),
                LocalDateTime.now()
        );

        List<Map<String, Object>> eventResponses = pastEvents.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(eventResponses);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Map<String, Object>> getEventBySlug(@PathVariable String slug) {
        CompanyModuleMapper companyModuleMapper = websiteManager.checkAuthentication();

        WebEvent event = webEventRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Verify event belongs to company and is published
        if (!event.getCompany().getId().equals(companyModuleMapper.getCompany().getId()) ||
                !Boolean.TRUE.equals(event.getPublished()) ||
                Boolean.TRUE.equals(event.getArchived())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(event));
    }

    private Map<String, Object> mapToResponse(WebEvent event) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", event.getId());
        response.put("eventKey", event.getEventKey());
        response.put("title", event.getTitle());
        response.put("slug", event.getSlug());
        response.put("description", event.getDescription());
        response.put("startDate", event.getStartDate());
        response.put("endDate", event.getEndDate());
        response.put("location", event.getLocation());
        response.put("coordinates", event.getCoordinates());
        response.put("embedCode", event.getEmbedCode());
        response.put("thumbnail", event.getPoster() != null ? event.getPoster().getLocation() : null);
        if (event.getContacts() != null) {
            response.put("contacts", event.getContacts().stream()
                    .filter(contact -> !Boolean.TRUE.equals(contact.getArchived()))
                    .map(contact -> {
                        Map<String, Object> contactMap = new HashMap<>();
                        contactMap.put("id", contact.getId());
                        contactMap.put("name", contact.getName());
                        contactMap.put("role", contact.getRole());
                        contactMap.put("email", contact.getEmail());
                        return contactMap;
                    })
                    .collect(Collectors.toList()));
        }
        return response;
    }
}

