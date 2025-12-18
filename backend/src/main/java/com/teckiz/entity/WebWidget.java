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
@Table(name = "WebWidget")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebWidget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "widget_key", length = 255)
    private String widgetKey;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "widget_type", length = 255)
    private String widgetType;

    @Column(name = "position", length = 255)
    private String position;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_module_mapper_id")
    private CompanyModuleMapper companyModuleMapper;

    @OneToMany(mappedBy = "widget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WidgetContent> contents = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (widgetKey == null) {
            widgetKey = UtilHelper.generateEntityKey();
        }
    }
}

