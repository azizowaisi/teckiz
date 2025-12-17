package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.WebNews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebNewsRepository extends JpaRepository<WebNews, Long> {

    Optional<WebNews> findByNewsKey(String newsKey);

    Optional<WebNews> findBySlug(String slug);

    List<WebNews> findByCompany(Company company);

    Page<WebNews> findByCompany(Company company, Pageable pageable);

    Page<WebNews> findByCompanyAndPublishedTrueAndArchivedFalse(Company company, Pageable pageable);

    @Query("SELECT n FROM WebNews n WHERE n.company = :company " +
           "AND n.published = true AND n.archived = false " +
           "AND (n.publishedAt IS NULL OR n.publishedAt <= :now) " +
           "ORDER BY n.publishedAt DESC")
    List<WebNews> findPublishedNews(@Param("company") Company company, @Param("now") LocalDateTime now);

    List<WebNews> findByCompanyAndCarouselTrueAndPublishedTrue(Company company);
}

