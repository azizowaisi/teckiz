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
@Table(name = "WebNews", indexes = {
    @Index(name = "web_news_slug", columnList = "slug"),
    @Index(name = "web_news_key_index", columnList = "news_key, published_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "news_key", length = 255)
    private String newsKey;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "slug", length = 64, unique = true)
    private String slug;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean archived = false;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean published = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "carousel")
    @Builder.Default
    private Boolean carousel = false;

    @Column(name = "embed_code", columnDefinition = "TEXT")
    private String embedCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poster_id")
    private WebRelatedMedia poster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "web_news_type_id")
    private WebNewsType webNewsType;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "WebNewsContactMapper",
        joinColumns = @JoinColumn(name = "news_id"),
        inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    @OrderBy("position DESC")
    @Builder.Default
    private List<WebContacts> contacts = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (newsKey == null) {
            newsKey = UtilHelper.generateEntityKey();
        }
    }
}

