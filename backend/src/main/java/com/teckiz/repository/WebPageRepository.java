package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebPageRepository extends JpaRepository<WebPage, Long> {

    Optional<WebPage> findByPageKey(String pageKey);

    Optional<WebPage> findBySlug(String slug);

    List<WebPage> findByCompany(Company company);

    Page<WebPage> findByCompany(Company company, Pageable pageable);

    List<WebPage> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);

    Page<WebPage> findByCompanyAndSlugContaining(Company company, String slug, Pageable pageable);
}

