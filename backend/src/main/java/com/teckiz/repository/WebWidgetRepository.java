package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebWidget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebWidgetRepository extends JpaRepository<WebWidget, Long> {

    Optional<WebWidget> findByWidgetKey(String widgetKey);

    List<WebWidget> findByCompany(Company company);

    List<WebWidget> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);

    List<WebWidget> findByCompanyAndActiveTrue(Company company);

    List<WebWidget> findByCompanyModuleMapperAndActiveTrue(CompanyModuleMapper companyModuleMapper);

    List<WebWidget> findByCompanyModuleMapperAndPosition(CompanyModuleMapper companyModuleMapper, String position);
}

