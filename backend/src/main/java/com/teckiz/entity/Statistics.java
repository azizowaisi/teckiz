package com.teckiz.entity;

import com.teckiz.util.UtilHelper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Statistics", indexes = {
    @Index(name = "statistics_date_index", columnList = "recorded_at"),
    @Index(name = "statistics_type_index", columnList = "stat_type, company_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_key", length = 255)
    private String statKey;

    @Column(name = "stat_type", length = 100, nullable = false)
    private String statType; // e.g., "page_view", "article_view", "download", "search", "user_action"

    @Column(name = "entity_type", length = 100)
    private String entityType; // e.g., "WebPage", "ResearchArticle", "WebNews"

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_key", length = 255)
    private String entityKey;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "referrer", length = 500)
    private String referrer;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_module_mapper_id")
    private CompanyModuleMapper companyModuleMapper;

    @CreationTimestamp
    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        if (statKey == null) {
            statKey = UtilHelper.generateEntityKey();
        }
    }
}

