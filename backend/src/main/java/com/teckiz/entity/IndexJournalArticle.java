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
@Table(name = "IndexJournalArticle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexJournalArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_key", length = 255)
    private String articleKey;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "sub_title", length = 255)
    private String subTitle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "abstract_text", columnDefinition = "TEXT")
    private String abstractText;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "doi", length = 255)
    private String doi;

    @Column(name = "page_start")
    private Integer pageStart;

    @Column(name = "page_end")
    private Integer pageEnd;

    @Column(name = "slug", length = 64, unique = true)
    private String slug;

    @Column(name = "thumbnail", columnDefinition = "TEXT")
    private String thumbnail;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean published = false;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean archived = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_journal_id", nullable = false)
    private IndexJournal indexJournal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_journal_volume_id")
    private IndexJournalVolume indexJournalVolume;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (articleKey == null) {
            articleKey = UtilHelper.generateEntityKey();
        }
    }
}

