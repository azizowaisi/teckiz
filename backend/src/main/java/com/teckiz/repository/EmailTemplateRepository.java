package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByTemplateKey(String templateKey);

    Optional<EmailTemplate> findByTemplateType(String templateType);

    Optional<EmailTemplate> findByTemplateTypeAndCompany(String templateType, Company company);

    Optional<EmailTemplate> findByTemplateTypeAndCompanyModuleMapper(String templateType, CompanyModuleMapper companyModuleMapper);

    List<EmailTemplate> findByCompany(Company company);

    List<EmailTemplate> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);

    List<EmailTemplate> findByActiveTrue();

    List<EmailTemplate> findByCompanyAndActiveTrue(Company company);
}

