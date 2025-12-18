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
@Table(name = "ResearchJournalVolume", indexes = {
    @Index(name = "research_journal_volume_slug", columnList = "slug")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchJournalVolume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "volume_key", length = 50)
    private String volumeKey;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean published = false;

    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private Boolean archived = false;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @Column(name = "number")
    private Integer number;

    @Column(name = "vol_number")
    private Integer volumeNumber;

    @Column(name = "issue_number")
    private Integer issueNumber;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "sub_title", length = 255)
    private String subTitle;

    @Column(name = "slug", length = 64, unique = true)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail", columnDefinition = "TEXT")
    private String thumbnail;

    @Column(name = "publisher", length = 255)
    private String publisher;

    @Column(name = "language", length = 50)
    private String language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_journal_id", nullable = false)
    private ResearchJournal researchJournal;

    @PrePersist
    protected void onCreate() {
        if (volumeKey == null) {
            volumeKey = UtilHelper.generateEntityKey();
        }
    }
}

