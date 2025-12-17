package com.teckiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.teckiz.util.UtilHelper;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "User")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_key", length = 255)
    private String userKey;

    @Column(name = "email", length = 64, nullable = false, unique = true)
    private String email;

    @Column(name = "name", length = 48, nullable = false)
    private String name;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "salt", length = 32)
    private String salt;

    @Column(name = "roles", length = 255)
    private String roles;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "is_password_temporary")
    @Builder.Default
    private Boolean isPasswordTemporary = false;

    @Column(name = "is_super_admin")
    @Builder.Default
    private Boolean isSuperAdmin = false;

    @Column(name = "is_deactive", nullable = false)
    @Builder.Default
    private Boolean isDeactive = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Profile profile;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "UserCompanyRole",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> rolesSet = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (userKey == null) {
            userKey = UtilHelper.generateEntityKey();
        }
    }

    public Set<String> getRoleNames() {
        Set<String> roleNames = new HashSet<>();
        if (isSuperAdmin != null && isSuperAdmin) {
            roleNames.add("ROLE_SUPER_ADMIN");
        }
        if (rolesSet != null) {
            rolesSet.forEach(role -> roleNames.add("ROLE_" + role.getName()));
        }
        if (roleNames.isEmpty()) {
            roleNames.add("ROLE_USER");
        }
        return roleNames;
    }
}

