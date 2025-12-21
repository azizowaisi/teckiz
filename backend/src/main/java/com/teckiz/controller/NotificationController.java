package com.teckiz.controller;

import com.teckiz.entity.Notification;
import com.teckiz.entity.User;
import com.teckiz.repository.NotificationRepository;
import com.teckiz.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPANY_ADMIN', 'COMPANY_AUTHOR', 'COMPANY_REVIEWER')")
@Tag(name = "Notifications", description = "User notification management endpoints")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean read) {

        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notifications;

        if (read != null) {
            if (read) {
                notifications = notificationRepository.findByUser(user, pageable);
            } else {
                notifications = notificationRepository.findByUserAndReadFalse(user, pageable);
            }
        } else {
            notifications = notificationRepository.findByUser(user, pageable);
        }

        List<Map<String, Object>> notificationResponses = notifications.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notificationResponses);
        response.put("totalPages", notifications.getTotalPages());
        response.put("totalElements", notifications.getTotalElements());
        response.put("currentPage", page);
        response.put("unreadCount", notificationRepository.countByUserAndReadFalse(user));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        User user = getCurrentUser();
        Long count = notificationRepository.countByUserAndReadFalse(user);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @GetMapping("/{notificationKey}")
    public ResponseEntity<Map<String, Object>> getNotification(@PathVariable String notificationKey) {
        User user = getCurrentUser();

        Notification notification = notificationRepository.findByNotificationKey(notificationKey)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(mapToResponse(notification));
    }

    @PutMapping("/{notificationKey}/read")
    public ResponseEntity<?> markAsRead(@PathVariable String notificationKey) {
        User user = getCurrentUser();

        Notification notification = notificationRepository.findByNotificationKey(notificationKey)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);

        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        User user = getCurrentUser();

        List<Notification> unreadNotifications = notificationRepository.findByUserAndReadFalse(user);
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unreadNotifications);

        return ResponseEntity.ok(Map.of("message", "All notifications marked as read", "count", unreadNotifications.size()));
    }

    @DeleteMapping("/{notificationKey}")
    public ResponseEntity<?> deleteNotification(@PathVariable String notificationKey) {
        User user = getCurrentUser();

        Notification notification = notificationRepository.findByNotificationKey(notificationKey)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        notificationRepository.delete(notification);

        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }

    private User getCurrentUser() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findOneByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Map<String, Object> mapToResponse(Notification notification) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", notification.getId());
        response.put("notificationKey", notification.getNotificationKey());
        response.put("title", notification.getTitle());
        response.put("message", notification.getMessage());
        response.put("type", notification.getType());
        response.put("read", notification.getRead());
        response.put("readAt", notification.getReadAt());
        response.put("actionUrl", notification.getActionUrl());
        response.put("actionText", notification.getActionText());
        response.put("createdAt", notification.getCreatedAt());
        response.put("updatedAt", notification.getUpdatedAt());
        return response;
    }
}

