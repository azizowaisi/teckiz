package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.ResearchArticleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResearchArticleTypeRepository extends JpaRepository<ResearchArticleType, Long> {

    Optional<ResearchArticleType> findByTypeKey(String typeKey);

    List<ResearchArticleType> findByCompany(Company company);
}

