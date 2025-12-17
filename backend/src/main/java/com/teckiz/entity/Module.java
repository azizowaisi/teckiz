package com.teckiz.entity;

import com.teckiz.util.UtilHelper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Module")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Module {

    public static final String WEBSITE = "website";
    public static final String EDUCATION = "education";
    public static final String JOURNAL = "journal";
    public static final String JOURNAL_INDEX = "rj-index";
    public static final String REVIEW = "review-and-submission";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_key", length = 255)
    private String moduleKey;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "type", length = 255)
    private String type;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean archived = false;

    @PrePersist
    protected void onCreate() {
        if (moduleKey == null) {
            moduleKey = UtilHelper.generateEntityKey();
        }
    }
}

