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
@Table(name = "PasswordSecrecy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordSecrecy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_key", length = 255)
    private String userKey;

    @Column(name = "secret_key", length = 255)
    private String secretKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "email", length = 64)
    private String email;

    @Column(name = "cursor_value", length = 64)
    private String cursor;

    @Column(name = "complete_list_size", length = 64)
    private String completeListSize;

    @Column(name = "path", length = 255)
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_module_mapper_id")
    private CompanyModuleMapper companyModuleMapper;

    @PrePersist
    protected void onCreate() {
        if (secretKey == null) {
            secretKey = UtilHelper.generateEntityKey();
        }
    }
}

