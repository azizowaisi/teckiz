package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebSubscriberRepository extends JpaRepository<WebSubscriber, Long> {

    Optional<WebSubscriber> findBySubscriberKey(String subscriberKey);

    Optional<WebSubscriber> findByEmail(String email);

    List<WebSubscriber> findByCompany(Company company);

    List<WebSubscriber> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);

    List<WebSubscriber> findByCompanyAndActiveTrue(Company company);

    Optional<WebSubscriber> findByCompanyAndEmail(Company company, String email);
}

