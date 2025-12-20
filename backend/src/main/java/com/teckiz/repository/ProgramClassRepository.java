package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.ProgramClass;
import com.teckiz.entity.ProgramCourse;
import com.teckiz.entity.ProgramTerm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramClassRepository extends JpaRepository<ProgramClass, Long> {

    Optional<ProgramClass> findByClassKey(String classKey);

    List<ProgramClass> findByCompany(Company company);

    Page<ProgramClass> findByCompany(Company company, Pageable pageable);

    List<ProgramClass> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);

    List<ProgramClass> findByCompanyAndPublishedTrueAndArchivedFalse(Company company);

    Page<ProgramClass> findByCompanyAndPublishedTrueAndArchivedFalse(Company company, Pageable pageable);

    List<ProgramClass> findByProgramCourse(ProgramCourse programCourse);

    List<ProgramClass> findByProgramTerm(ProgramTerm programTerm);

    List<ProgramClass> findByCompanyAndProgramCourse(Company company, ProgramCourse programCourse);

    List<ProgramClass> findByCompanyAndProgramTerm(Company company, ProgramTerm programTerm);

    List<ProgramClass> findByCompanyAndProgramCourseAndProgramTerm(
            Company company, ProgramCourse programCourse, ProgramTerm programTerm);
}

