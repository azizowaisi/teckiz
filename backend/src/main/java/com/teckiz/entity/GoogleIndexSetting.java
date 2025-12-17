package com.teckiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "GoogleIndexSetting")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleIndexSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "is_google_indexing_active")
    @Builder.Default
    private Boolean googleIndexingActive = false;

    @Column(name = "google_site_verification_key", length = 255)
    private String googleSiteVerificationKey;

    @Column(name = "google_analytics_key", length = 255)
    private String googleAnalyticsKey;

    @OneToOne
    @JoinColumn(name = "company_module_mapper_id", nullable = false, unique = true)
    private CompanyModuleMapper companyModuleMapper;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

