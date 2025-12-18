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
@Table(name = "ResearchArticleReviewerMapper")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchArticleReviewerMapper {

    public static final String PENDING = "pending";
    public static final String APPROVED = "approved";
    public static final String CONDITIONAL_APPROVAL = "conditional-approval";
    public static final String REJECTED = "rejected";
    public static final String INPROGRESS = "inprogress";
    public static final String UNSANCTIONED = "unsanctioned";
    public static final String REVISION = "revision";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mapper_key", length = 255, nullable = false)
    private String mapperKey;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "status", length = 255, nullable = false)
    @Builder.Default
    private String status = PENDING;

    @Column(name = "response_due_at")
    private LocalDateTime responseDueAt;

    @Column(name = "review_due_at")
    private LocalDateTime reviewDueAt;

    @Column(name = "is_accepted_for_review", length = 255, nullable = false)
    private String acceptedForReview;

    @Column(name = "is_review_submitted", length = 255, nullable = false)
    private String reviewSubmitted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ResearchArticle article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_company_role_id")
    private UserCompanyRole reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "web_page_id")
    private WebPage page;

    @PrePersist
    protected void onCreate() {
        if (mapperKey == null) {
            mapperKey = UtilHelper.generateEntityKey();
        }
    }
}

