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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ResearchArticle", indexes = {
    @Index(name = "research_article_page_index", columnList = "article_key, status"),
    @Index(name = "research_article_search_index", columnList = "title, discipline, keywords"),
    @Index(name = "research_article_list_status_index", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchArticle {

    public static final String INCOMPLETE = "incomplete";
    public static final String SUBMITTED = "submitted";
    public static final String RECEIVED = "received";
    public static final String EVALUATING = "evaluating";
    public static final String APPROVED = "approved";
    public static final String UNSANCTIONED = "unsanctioned";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_key", length = 255)
    private String articleKey;

    @Column(name = "doaj_id", length = 255)
    private String doajId;

    @Column(name = "doaj_submitted")
    @Builder.Default
    private Boolean doajSubmitted = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean published = false;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "number")
    @Builder.Default
    private Integer number = 0;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "discipline", length = 255)
    private String discipline;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @Column(name = "status")
    @Builder.Default
    private String status = INCOMPLETE;

    @Column(name = "downloads", length = 50)
    private String downloads;

    @Column(name = "views", length = 50)
    private String views;

    @Column(name = "visits", length = 50)
    private String visits;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "title_language", length = 20)
    @Builder.Default
    private String titleLanguage = "eng";

    @Column(name = "english_title", length = 255)
    private String englishTitle;

    @Column(name = "slug", length = 64, unique = true)
    private String slug;

    @Column(name = "abstract", columnDefinition = "TEXT")
    private String abstractText;

    @Column(name = "abstract_language", length = 20)
    @Builder.Default
    private String abstractLanguage = "eng";

    @Column(name = "keywords", length = 255)
    private String keywords;

    @Column(name = "keywords_language", length = 20)
    @Builder.Default
    private String keywordsLanguage = "eng";

    @Column(name = "is_reference", columnDefinition = "TEXT")
    private String references;

    @Column(name = "references_language", length = 20)
    @Builder.Default
    private String referencesLanguage = "eng";

    @Column(name = "thumbnail", columnDefinition = "TEXT")
    private String thumbnail;

    @Column(name = "language", length = 255)
    private String language;

    @Column(name = "home")
    @Builder.Default
    private Boolean home = false;

    @Column(name = "page_number", length = 255)
    private String pageNumber;

    @Column(name = "start_page")
    private Integer startPage;

    @Column(name = "end_page")
    private Integer endPage;

    @Column(name = "is_conditions")
    @Builder.Default
    private Boolean conditions = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_article_type_id")
    private ResearchArticleType researchArticleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_journal_volume_id")
    private ResearchJournalVolume researchJournalVolume;

    @OneToMany(mappedBy = "researchArticle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @Builder.Default
    private List<ResearchArticleAuthorMapper> researchArticleAuthorMappers = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ResearchArticleReviewerMapper> researchArticleReviewerMappers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (articleKey == null) {
            articleKey = UtilHelper.generateEntityKey();
        }
    }
}

