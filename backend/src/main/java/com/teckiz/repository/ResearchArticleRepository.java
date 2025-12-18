package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.ResearchArticle;
import com.teckiz.entity.ResearchJournalVolume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResearchArticleRepository extends JpaRepository<ResearchArticle, Long> {

    Optional<ResearchArticle> findByArticleKey(String articleKey);

    Optional<ResearchArticle> findBySlug(String slug);

    List<ResearchArticle> findByCompany(Company company);

    Page<ResearchArticle> findByCompany(Company company, Pageable pageable);

    List<ResearchArticle> findByCompanyAndStatus(Company company, String status);

    Page<ResearchArticle> findByCompanyAndStatus(Company company, String status, Pageable pageable);

    List<ResearchArticle> findByCompanyAndPublishedTrue(Company company);

    Page<ResearchArticle> findByCompanyAndPublishedTrue(Company company, Pageable pageable);

    List<ResearchArticle> findByResearchJournalVolume(ResearchJournalVolume volume);

    Page<ResearchArticle> findByResearchJournalVolume(ResearchJournalVolume volume, Pageable pageable);

    @Query("SELECT a FROM ResearchArticle a WHERE a.company = :company " +
           "AND a.published = true " +
           "AND (a.title LIKE %:search% OR a.abstractText LIKE %:search% OR a.keywords LIKE %:search%)")
    List<ResearchArticle> searchPublishedArticles(@Param("company") Company company, @Param("search") String search);
}

