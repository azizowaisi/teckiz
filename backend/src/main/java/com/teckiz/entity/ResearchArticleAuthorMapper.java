package com.teckiz.entity;

import com.teckiz.util.UtilHelper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ResearchArticleAuthorMapper")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchArticleAuthorMapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_mapper_key", length = 255, nullable = false)
    private String authorMapperKey;

    @Column(name = "position")
    @Builder.Default
    private Integer position = 0;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean archived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_article_id", nullable = false)
    private ResearchArticle researchArticle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_article_author_id", nullable = false)
    private ResearchArticleAuthor researchArticleAuthor;

    @PrePersist
    protected void onCreate() {
        if (authorMapperKey == null) {
            authorMapperKey = UtilHelper.generateEntityKey();
        }
    }
}

