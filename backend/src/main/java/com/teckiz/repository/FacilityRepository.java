package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Facility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    Optional<Facility> findByFacilityKey(String facilityKey);

    List<Facility> findByCompany(Company company);

    Page<Facility> findByCompany(Company company, Pageable pageable);

    List<Facility> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);

    List<Facility> findByCompanyAndPublishedTrueAndArchivedFalse(Company company);

    Page<Facility> findByCompanyAndPublishedTrueAndArchivedFalse(Company company, Pageable pageable);
}

