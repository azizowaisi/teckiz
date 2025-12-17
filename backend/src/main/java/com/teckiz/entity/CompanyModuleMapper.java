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
@Table(name = "CompanyModuleMapper")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyModuleMapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_mapper_key", length = 50, nullable = false)
    private String moduleMapperKey;

    @Column(name = "directory", length = 50)
    private String directory;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "host", length = 50)
    private String host;

    @Column(name = "colors", columnDefinition = "TEXT")
    private String colors;

    @Column(name = "is_live")
    @Builder.Default
    private Boolean live = true;

    @Column(name = "is_master")
    @Builder.Default
    private Boolean master = false;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean archived = false;

    @Column(name = "header", length = 8)
    @Builder.Default
    private String header = "1";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @OneToMany(mappedBy = "companyModuleMapper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CompanyModuleMapperMenu> menuItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (moduleMapperKey == null) {
            moduleMapperKey = UtilHelper.generateEntityKey();
        }
        if (colors == null) {
            colors = getDefaultColorsJson();
        }
    }

    public String getDefaultColorsJson() {
        return "{\"top-bar\":\"#0A69AD\",\"top-bar-btn\":\"#ffffff\",\"footer\":\"#0A69AD\"," +
               "\"footer-btn\":\"#ffffff\",\"header-background\":\"#ffffff\",\"header-btn\":\"#ffffff\"," +
               "\"header-navbar\":\"#1C1474\",\"header-navbar-hover\":\"#ffffff\",\"theme\":\"#1C1474\"," +
               "\"hover\":\"#1C1474\",\"dark\":\"#05172e\"}";
    }
}

