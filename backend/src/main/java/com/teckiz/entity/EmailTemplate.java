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
@Table(name = "EmailTemplate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_key", length = 255)
    private String templateKey;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "subject", length = 500, nullable = false)
    private String subject;

    @Column(name = "html_body", columnDefinition = "TEXT", nullable = false)
    private String htmlBody;

    @Column(name = "text_body", columnDefinition = "TEXT")
    private String textBody;

    @Column(name = "template_type", length = 100)
    private String templateType; // e.g., "verification", "welcome", "password_reset", "notification"

    @Column(name = "variables", columnDefinition = "TEXT")
    private String variables; // JSON string of available variables

    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company; // null for global templates

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
        if (templateKey == null) {
            templateKey = UtilHelper.generateEntityKey();
        }
    }
}

