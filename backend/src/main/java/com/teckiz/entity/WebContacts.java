package com.teckiz.entity;

import com.teckiz.util.UtilHelper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "WebContacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebContacts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contact_key", length = 255)
    private String contactKey;

    @Column(name = "name", length = 255)
    private String name;

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

    @Column(name = "position")
    @Builder.Default
    private Integer position = 0;

    @Column(name = "location", length = 255)
    private String thumbnail;

    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private Boolean archived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "WebContactTypeMapper",
        joinColumns = @JoinColumn(name = "contact_id"),
        inverseJoinColumns = @JoinColumn(name = "contact_type_id")
    )
    @Builder.Default
    private List<WebContactType> contactTypes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (contactKey == null) {
            contactKey = UtilHelper.generateEntityKey();
        }
    }
}

