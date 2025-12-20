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
@Table(name = "ResearchRelatedMedia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchRelatedMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "related_media_key", length = 255, nullable = false)
    private String relatedMediaKey;

    @Column(name = "mimetype", length = 64)
    private String mimeType;

    @Column(name = "media_type", length = 64)
    private String mediaType;

    @Column(name = "location", columnDefinition = "TEXT")
    private String location;

    @Column(name = "is_poster")
    @Builder.Default
    private Boolean poster = false;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_article_id")
    private ResearchArticle researchArticle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_journal_id")
    private ResearchJournal researchJournal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_journal_volume_id")
    private ResearchJournalVolume researchJournalVolume;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (relatedMediaKey == null) {
            relatedMediaKey = UtilHelper.generateEntityKey();
        }
    }
}

