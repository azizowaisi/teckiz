package com.teckiz.repository;

import com.teckiz.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findBySlug(String slug);

    Optional<Company> findByName(String name);

    Optional<Company> findByCompanyKey(String companyKey);

    @Query("SELECT c FROM Company c WHERE c.archived = false ORDER BY c.name ASC")
    List<Company> findAllActiveCompanies();

    @Query("SELECT c FROM Company c WHERE c.archived = false AND c.active = true ORDER BY c.name ASC")
    List<Company> findAllActiveAndNotArchived();
}

