package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ProgramLevel;
import com.teckiz.entity.ProgramLevelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramLevelRepository extends JpaRepository<ProgramLevel, Long> {

    Optional<ProgramLevel> findByLevelKey(String levelKey);

    List<ProgramLevel> findByCompany(Company company);

    Page<ProgramLevel> findByCompany(Company company, Pageable pageable);

    List<ProgramLevel> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);

    List<ProgramLevel> findByCompanyAndActiveTrueAndArchivedFalse(Company company);

    Page<ProgramLevel> findByCompanyAndActiveTrueAndArchivedFalse(Company company, Pageable pageable);

    List<ProgramLevel> findByProgramLevelType(ProgramLevelType programLevelType);

    List<ProgramLevel> findByCompanyAndProgramLevelType(Company company, ProgramLevelType programLevelType);
}

