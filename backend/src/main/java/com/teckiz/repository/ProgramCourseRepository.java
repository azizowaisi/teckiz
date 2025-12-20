package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ProgramCourse;
import com.teckiz.entity.ProgramLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramCourseRepository extends JpaRepository<ProgramCourse, Long> {

    Optional<ProgramCourse> findByCourseKey(String courseKey);

    List<ProgramCourse> findByCompany(Company company);

    Page<ProgramCourse> findByCompany(Company company, Pageable pageable);

    List<ProgramCourse> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);

    List<ProgramCourse> findByCompanyAndPublishedTrueAndArchivedFalse(Company company);

    Page<ProgramCourse> findByCompanyAndPublishedTrueAndArchivedFalse(Company company, Pageable pageable);

    List<ProgramCourse> findByProgramLevel(ProgramLevel programLevel);

    List<ProgramCourse> findByCompanyAndProgramLevel(Company company, ProgramLevel programLevel);

    Optional<ProgramCourse> findByCompanyAndCode(Company company, String code);
}

