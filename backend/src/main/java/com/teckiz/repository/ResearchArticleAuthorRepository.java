package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.ResearchArticleAuthor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResearchArticleAuthorRepository extends JpaRepository<ResearchArticleAuthor, Long> {

    Optional<ResearchArticleAuthor> findByAuthorKey(String authorKey);

    List<ResearchArticleAuthor> findByCompany(Company company);

    List<ResearchArticleAuthor> findByCompanyAndEmail(Company company, String email);
}

