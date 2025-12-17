package com.teckiz.entity;

import com.teckiz.util.UtilHelper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    public static final String ROLE_COMPANY_ADMIN = "ROLE_COMPANY_ADMIN";
    public static final String ROLE_COMPANY_AUTHOR = "ROLE_COMPANY_AUTHOR";
    public static final String ROLE_COMPANY_REVIEWER = "ROLE_COMPANY_REVIEWER";
    public static final String ROLE_COMPANY_USER = "ROLE_COMPANY_USER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "role_key", length = 255, nullable = false)
    private String roleKey;

    @Column(name = "role", length = 255, unique = true)
    private String role;

    @Column(name = "is_company_role")
    @Builder.Default
    private Boolean companyRole = true;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "rolesSet")
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (roleKey == null) {
            roleKey = UtilHelper.generateEntityKey();
        }
    }
}

