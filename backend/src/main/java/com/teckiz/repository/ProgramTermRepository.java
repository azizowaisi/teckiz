package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ProgramTerm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramTermRepository extends JpaRepository<ProgramTerm, Long> {

    Optional<ProgramTerm> findByTermKey(String termKey);

    List<ProgramTerm> findByCompany(Company company);

    Page<ProgramTerm> findByCompany(Company company, Pageable pageable);

    List<ProgramTerm> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);

    List<ProgramTerm> findByCompanyAndActiveTrueAndArchivedFalse(Company company);

    Page<ProgramTerm> findByCompanyAndActiveTrueAndArchivedFalse(Company company, Pageable pageable);

    List<ProgramTerm> findByCompanyAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Company company, LocalDateTime date, LocalDateTime date2);
}

