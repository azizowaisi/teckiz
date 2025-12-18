package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.WebRelatedMedia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebRelatedMediaRepository extends JpaRepository<WebRelatedMedia, Long> {

    Optional<WebRelatedMedia> findByRelatedMediaKey(String relatedMediaKey);

    List<WebRelatedMedia> findByCompany(Company company);

    Page<WebRelatedMedia> findByCompany(Company company, Pageable pageable);

    List<WebRelatedMedia> findByCompanyAndPosterTrue(Company company);

    Page<WebRelatedMedia> findByCompanyAndPosterTrue(Company company, Pageable pageable);
}

