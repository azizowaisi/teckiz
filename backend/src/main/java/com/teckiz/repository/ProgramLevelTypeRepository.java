package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ProgramLevelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramLevelTypeRepository extends JpaRepository<ProgramLevelType, Long> {

    Optional<ProgramLevelType> findByTypeKey(String typeKey);

    List<ProgramLevelType> findByCompany(Company company);

    Page<ProgramLevelType> findByCompany(Company company, Pageable pageable);

    List<ProgramLevelType> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);
}

