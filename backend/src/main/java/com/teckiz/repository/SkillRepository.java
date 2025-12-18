package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Skill;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findBySkillKey(String skillKey);

    List<Skill> findByCompany(Company company);

    List<Skill> findByCompany(Company company, Sort sort);

    List<Skill> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);

    List<Skill> findByCompanyAndArchivedFalse(Company company, Sort sort);
}

