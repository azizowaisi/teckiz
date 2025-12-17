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
@Table(name = "WebRelatedMedia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebRelatedMedia {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

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

