package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.PrincipalMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrincipalMessageRepository extends JpaRepository<PrincipalMessage, Long> {

    Optional<PrincipalMessage> findByMessageKey(String messageKey);

    List<PrincipalMessage> findByCompany(Company company);

    List<PrincipalMessage> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);

    Optional<PrincipalMessage> findByCompanyAndPublishedTrue(Company company);

    Optional<PrincipalMessage> findByCompanyModuleMapperAndPublishedTrue(CompanyModuleMapper companyModuleMapper);
}

