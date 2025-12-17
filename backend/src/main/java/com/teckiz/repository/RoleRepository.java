package com.teckiz.repository;

import com.teckiz.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    List<Role> findByCompanyRoleTrue();

    Optional<Role> findByRoleKey(String roleKey);

    List<Role> findAll();
}

