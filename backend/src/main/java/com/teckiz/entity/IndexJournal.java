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
@Table(name = "IndexJournal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexJournal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "journal_key", length = 255)
    private String journalKey;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "abbreviation", length = 255)
    private String abbreviation;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "sub_title", length = 255)
    private String subTitle;

    @Column(name = "print_issn", length = 255)
    private String printISSN;

    @Column(name = "online_issn", length = 255)
    private String onlineISSN;

    @Column(name = "slug", length = 64, unique = true)
    private String slug;

    @Column(name = "thumbnail", columnDefinition = "TEXT")
    private String thumbnail;

    @Column(name = "publisher", length = 255)
    private String publisher;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean archived = false;

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
        if (journalKey == null) {
            journalKey = UtilHelper.generateEntityKey();
        }
    }
}

