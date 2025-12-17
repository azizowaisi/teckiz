package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyRoleMapper;
import com.teckiz.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRoleMapperRepository extends JpaRepository<CompanyRoleMapper, Long> {

    List<CompanyRoleMapper> findByCompany(Company company);

    Optional<CompanyRoleMapper> findByCompanyAndRoleAndArchivedFalse(Company company, Role role);

    Optional<CompanyRoleMapper> findByCompanyRoleKeyAndArchivedFalse(String companyRoleKey);

    @Query("SELECT crm FROM CompanyRoleMapper crm " +
           "JOIN crm.role r " +
           "WHERE r.roleKey = :roleKey")
    Optional<CompanyRoleMapper> findByRoleKey(@Param("roleKey") String roleKey);

    List<CompanyRoleMapper> findByCompanyAndArchivedFalse(Company company);
}

