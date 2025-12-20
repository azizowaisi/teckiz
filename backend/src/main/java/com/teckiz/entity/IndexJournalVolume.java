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
@Table(name = "IndexJournalVolume")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexJournalVolume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "volume_key", length = 255)
    private String volumeKey;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "sub_title", length = 255)
    private String subTitle;

    @Column(name = "number")
    private Integer number;

    @Column(name = "vol_number")
    private Integer volumeNumber;

    @Column(name = "issue_number")
    private Integer issueNumber;

    @Column(name = "slug", length = 64, unique = true)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail", columnDefinition = "TEXT")
    private String thumbnail;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean published = false;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean archived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_journal_id", nullable = false)
    private IndexJournal indexJournal;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (volumeKey == null) {
            volumeKey = UtilHelper.generateEntityKey();
        }
    }
}

