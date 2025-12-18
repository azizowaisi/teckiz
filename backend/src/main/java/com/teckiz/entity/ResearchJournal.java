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
@Table(name = "ResearchJournal", indexes = {
    @Index(name = "research_journal_slug", columnList = "slug")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchJournal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "journal_key", length = 255)
    private String journalKey;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_submission_activated")
    @Builder.Default
    private Boolean submissionActivated = false;

    @Column(name = "required_votes")
    @Builder.Default
    private Integer requiredVotes = 0;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "abbreviation", length = 255)
    private String abbreviation;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "title_language", length = 20)
    @Builder.Default
    private String titleLanguage = "eng";

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

    @Column(name = "faculty", length = 255)
    private String faculty;

    @Column(name = "start_year", length = 50)
    private String startYear;

    @Column(name = "online_start_year", length = 50)
    private String onlineStartYear;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "licence", columnDefinition = "TEXT")
    private String licence;

    @Column(name = "creative_licence_type", length = 50)
    @Builder.Default
    private String creativeLicenceType = "cc-by";

    @Column(name = "doaj_api_key", length = 255)
    private String doajApiKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @PrePersist
    protected void onCreate() {
        if (journalKey == null) {
            journalKey = UtilHelper.generateEntityKey();
        }
    }
}

