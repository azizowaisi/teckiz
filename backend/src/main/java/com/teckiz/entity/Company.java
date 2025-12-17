package com.teckiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.teckiz.util.UtilHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Company")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_key", length = 255, nullable = false)
    private String companyKey;

    @Column(name = "name", length = 255, unique = true)
    private String name;

    @Column(name = "slug", length = 64, unique = true, nullable = false)
    private String slug;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "stripe_id", length = 255)
    private String stripeId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "aboutus", columnDefinition = "TEXT")
    private String aboutus;

    @Column(name = "city", length = 255)
    private String city;

    @Column(name = "country", length = 2)
    private String country;

    @Column(name = "time_zone", length = 255)
    private String timeZone;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = false;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean archived = false;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 255)
    private String phone;

    @Column(name = "map_location")
    private String location;

    @Column(name = "map_coordinates")
    private String coordinates;

    @Column(name = "logo", columnDefinition = "TEXT")
    private String logo;

    @Column(name = "logo_size", columnDefinition = "TEXT")
    private String logoSize;

    @Column(name = "favicon", columnDefinition = "TEXT")
    private String favicon;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "workingdays", length = 255)
    private String workingdays;

    @Column(name = "holidays", length = 255)
    private String holidays;

    @Column(name = "privacy_policy", columnDefinition = "TEXT")
    private String privacyPolicy;

    @Column(name = "lang", length = 10)
    private String lang;

    @Column(name = "is_master")
    private Boolean master;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CompanyRoleMapper> roles = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CompanyModuleMapper> modules = new ArrayList<>();

    @OneToMany(mappedBy = "masterCompany", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Company> subCompanies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_company_id")
    private Company masterCompany;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (companyKey == null) {
            companyKey = UtilHelper.generateEntityKey();
        }
    }
}

