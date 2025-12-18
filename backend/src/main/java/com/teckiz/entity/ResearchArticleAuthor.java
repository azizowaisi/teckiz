package com.teckiz.entity;

import com.teckiz.util.UtilHelper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ResearchArticleAuthor", indexes = {
    @Index(name = "research_author_search_index", columnList = "name, role, email, phone")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchArticleAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_key", length = 255)
    private String authorKey;

    @Column(name = "orcid", length = 255)
    private String orcid;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "first_name", length = 255)
    private String firstname;

    @Column(name = "last_name", length = 255)
    private String lastname;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "url", length = 255)
    private String url;

    @Column(name = "role", length = 255)
    private String role;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 255)
    private String phone;

    @Column(name = "linkedin", length = 255)
    private String linkedin;

    @Column(name = "researchgate", length = 255)
    private String researchGate;

    @Column(name = "twitter", length = 255)
    private String twitter;

    @Column(name = "facebook", length = 255)
    private String facebook;

    @Column(name = "instagram", length = 255)
    private String instagram;

    @Column(name = "country", length = 255)
    private String country;

    @Column(name = "location", length = 255)
    private String thumbnail;

    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private Boolean archived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @PrePersist
    protected void onCreate() {
        if (authorKey == null) {
            authorKey = UtilHelper.generateEntityKey();
        }
    }
}

