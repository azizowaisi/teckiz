package com.teckiz.controller;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebWidget;
import com.teckiz.entity.WidgetContent;
import com.teckiz.repository.WebWidgetRepository;
import com.teckiz.repository.WidgetContentRepository;
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
@RequestMapping("/website/admin/widgets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_AUTHOR')")
public class WebWidgetController {

    private final ModuleAccessManager moduleAccessManager;
    private final WebWidgetRepository widgetRepository;
    private final WidgetContentRepository contentRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listWidgets(
            @RequestParam(required = false) String position) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        List<WebWidget> widgets;
        if (position != null && !position.isEmpty()) {
            widgets = widgetRepository.findByCompanyModuleMapperAndPosition(
                    companyModuleMapper, position);
        } else {
            widgets = widgetRepository.findByCompanyModuleMapper(companyModuleMapper);
        }

        List<Map<String, Object>> widgetResponses = widgets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("widgets", widgetResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{widgetKey}")
    public ResponseEntity<Map<String, Object>> getWidget(@PathVariable String widgetKey) {
        moduleAccessManager.authenticateModule();

        return widgetRepository.findByWidgetKey(widgetKey)
                .map(widget -> ResponseEntity.ok(mapToResponse(widget)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createWidget(@RequestBody Map<String, Object> request) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        String name = (String) request.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }

        WebWidget widget = WebWidget.builder()
                .company(companyModuleMapper.getCompany())
                .companyModuleMapper(companyModuleMapper)
                .name(name)
                .widgetType((String) request.get("widgetType"))
                .position((String) request.get("position"))
                .active(request.get("active") != null ?
                        (Boolean) request.get("active") : true)
                .build();

        widget = widgetRepository.save(widget);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Widget created successfully", "widgetKey", widget.getWidgetKey()));
    }

    @PutMapping("/{widgetKey}")
    public ResponseEntity<?> updateWidget(
            @PathVariable String widgetKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebWidget widget = widgetRepository.findByWidgetKey(widgetKey)
                .orElseThrow(() -> new RuntimeException("Widget not found"));

        if (!widget.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("name") != null) {
            widget.setName((String) request.get("name"));
        }
        if (request.get("widgetType") != null) {
            widget.setWidgetType((String) request.get("widgetType"));
        }
        if (request.get("position") != null) {
            widget.setPosition((String) request.get("position"));
        }
        if (request.get("active") != null) {
            widget.setActive((Boolean) request.get("active"));
        }

        widget = widgetRepository.save(widget);

        return ResponseEntity.ok(Map.of("message", "Widget updated successfully"));
    }

    @DeleteMapping("/{widgetKey}")
    public ResponseEntity<?> deleteWidget(@PathVariable String widgetKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebWidget widget = widgetRepository.findByWidgetKey(widgetKey)
                .orElseThrow(() -> new RuntimeException("Widget not found"));

        if (!widget.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        widgetRepository.delete(widget);

        return ResponseEntity.ok(Map.of("message", "Widget deleted successfully"));
    }

    @GetMapping("/{widgetKey}/contents")
    public ResponseEntity<Map<String, Object>> getWidgetContents(@PathVariable String widgetKey) {
        moduleAccessManager.authenticateModule();

        WebWidget widget = widgetRepository.findByWidgetKey(widgetKey)
                .orElseThrow(() -> new RuntimeException("Widget not found"));

        List<WidgetContent> contents = contentRepository.findByWidgetAndActiveTrueOrderByPositionAsc(widget);

        List<Map<String, Object>> contentResponses = contents.stream()
                .map(this::mapContentToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("contents", contentResponses);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{widgetKey}/contents")
    public ResponseEntity<?> createWidgetContent(
            @PathVariable String widgetKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WebWidget widget = widgetRepository.findByWidgetKey(widgetKey)
                .orElseThrow(() -> new RuntimeException("Widget not found"));

        if (!widget.getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WidgetContent content = WidgetContent.builder()
                .widget(widget)
                .title((String) request.get("title"))
                .content((String) request.get("content"))
                .image((String) request.get("image"))
                .link((String) request.get("link"))
                .position(request.get("position") != null ?
                        ((Number) request.get("position")).intValue() : 0)
                .active(request.get("active") != null ?
                        (Boolean) request.get("active") : true)
                .build();

        content = contentRepository.save(content);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Widget content created successfully", "contentKey", content.getContentKey()));
    }

    @PutMapping("/contents/{contentKey}")
    public ResponseEntity<?> updateWidgetContent(
            @PathVariable String contentKey,
            @RequestBody Map<String, Object> request) {

        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WidgetContent content = contentRepository.findByContentKey(contentKey)
                .orElseThrow(() -> new RuntimeException("Widget content not found"));

        if (!content.getWidget().getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.get("title") != null) {
            content.setTitle((String) request.get("title"));
        }
        if (request.get("content") != null) {
            content.setContent((String) request.get("content"));
        }
        if (request.get("image") != null) {
            content.setImage((String) request.get("image"));
        }
        if (request.get("link") != null) {
            content.setLink((String) request.get("link"));
        }
        if (request.get("position") != null) {
            content.setPosition(((Number) request.get("position")).intValue());
        }
        if (request.get("active") != null) {
            content.setActive((Boolean) request.get("active"));
        }

        content = contentRepository.save(content);

        return ResponseEntity.ok(Map.of("message", "Widget content updated successfully"));
    }

    @DeleteMapping("/contents/{contentKey}")
    public ResponseEntity<?> deleteWidgetContent(@PathVariable String contentKey) {
        CompanyModuleMapper companyModuleMapper = moduleAccessManager.authenticateModule();

        WidgetContent content = contentRepository.findByContentKey(contentKey)
                .orElseThrow(() -> new RuntimeException("Widget content not found"));

        if (!content.getWidget().getCompany().getId().equals(companyModuleMapper.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        contentRepository.delete(content);

        return ResponseEntity.ok(Map.of("message", "Widget content deleted successfully"));
    }

    private Map<String, Object> mapToResponse(WebWidget widget) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", widget.getId());
        response.put("widgetKey", widget.getWidgetKey());
        response.put("name", widget.getName());
        response.put("widgetType", widget.getWidgetType());
        response.put("position", widget.getPosition());
        response.put("active", widget.getActive());
        response.put("createdAt", widget.getCreatedAt());
        response.put("updatedAt", widget.getUpdatedAt());
        
        if (widget.getContents() != null) {
            response.put("contentCount", widget.getContents().size());
        }
        
        return response;
    }

    private Map<String, Object> mapContentToResponse(WidgetContent content) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", content.getId());
        response.put("contentKey", content.getContentKey());
        response.put("title", content.getTitle());
        response.put("content", content.getContent());
        response.put("image", content.getImage());
        response.put("link", content.getLink());
        response.put("position", content.getPosition());
        response.put("active", content.getActive());
        response.put("createdAt", content.getCreatedAt());
        response.put("updatedAt", content.getUpdatedAt());
        return response;
    }
}

