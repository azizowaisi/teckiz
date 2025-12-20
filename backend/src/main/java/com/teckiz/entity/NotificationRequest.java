package com.teckiz.entity;

import com.teckiz.util.UtilHelper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "NotificationRequest")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_key", length = 255)
    private String requestKey;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "type", length = 50)
    private String type; // e.g., "info", "warning", "error", "success"

    @Column(name = "target_type", length = 50)
    private String targetType; // e.g., "user", "company", "role", "all"

    @Column(name = "target_id")
    private Long targetId; // User ID, Company ID, or Role ID depending on targetType

    @Column(name = "target_key", length = 255)
    private String targetKey; // Alternative to targetId

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "action_text", length = 255)
    private String actionText;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "pending"; // pending, processing, completed, failed

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor; // For scheduled notifications

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_module_mapper_id")
    private CompanyModuleMapper companyModuleMapper;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (requestKey == null) {
            requestKey = UtilHelper.generateEntityKey();
        }
    }
}

