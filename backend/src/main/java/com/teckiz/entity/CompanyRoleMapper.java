package com.teckiz.entity;

import com.teckiz.util.UtilHelper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CompanyRoleMapper")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRoleMapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_role_key", length = 255, nullable = false)
    private String companyRoleKey;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean archived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @PrePersist
    protected void onCreate() {
        if (companyRoleKey == null) {
            companyRoleKey = UtilHelper.generateEntityKey();
        }
    }
}

