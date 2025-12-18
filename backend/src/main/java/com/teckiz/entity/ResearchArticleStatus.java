package com.teckiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ResearchArticleStatus")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchArticleStatus {

    public static final String STATUS = "status";
    public static final String MESSAGE = "message";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "category", length = 255)
    private String category;

    @Column(name = "status", length = 255)
    private String status;

    @Column(name = "subject", length = 255)
    private String subject;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ResearchArticle article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_company_role_id")
    private UserCompanyRole fromUserCompanyRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_company_role_id")
    private UserCompanyRole toUserCompanyRole;
}

