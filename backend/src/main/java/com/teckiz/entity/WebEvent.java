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
@Table(name = "WebEvent", indexes = {
    @Index(name = "web_event_slug", columnList = "slug")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_key", length = 255)
    private String eventKey;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "slug", length = 255, unique = true)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

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

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean archived = false;

    @Column(name = "map_location")
    private String location;

    @Column(name = "map_coordinates")
    private String coordinates;

    @Column(name = "embed_code", columnDefinition = "TEXT")
    private String embedCode;

    @Column(name = "carousel")
    @Builder.Default
    private Boolean carousel = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poster_id")
    private WebRelatedMedia poster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "WebEventContactMapper",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    @OrderBy("position DESC")
    @Builder.Default
    private List<WebContacts> contacts = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (eventKey == null) {
            eventKey = UtilHelper.generateEntityKey();
        }
        if (startDate == null) {
            startDate = LocalDateTime.now();
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
    }
}

