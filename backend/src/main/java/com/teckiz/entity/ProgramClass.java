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

@Entity
@Table(name = "ProgramClass")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_key", length = 255)
    private String classKey;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "room", length = 255)
    private String room;

    @Column(name = "schedule", length = 255)
    private String schedule;

    @Column(name = "instructor", length = 255)
    private String instructor;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "enrolled")
    @Builder.Default
    private Integer enrolled = 0;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean published = false;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean archived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_module_mapper_id")
    private CompanyModuleMapper companyModuleMapper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_course_id")
    private ProgramCourse programCourse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_term_id")
    private ProgramTerm programTerm;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (classKey == null) {
            classKey = UtilHelper.generateEntityKey();
        }
    }
}

