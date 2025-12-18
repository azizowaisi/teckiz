package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.WebEvent;
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
public interface WebEventRepository extends JpaRepository<WebEvent, Long> {

    Optional<WebEvent> findByEventKey(String eventKey);

    Optional<WebEvent> findBySlug(String slug);

    List<WebEvent> findByCompany(Company company);

    Page<WebEvent> findByCompany(Company company, Pageable pageable);

    Page<WebEvent> findByCompanyAndPublishedTrueAndArchivedFalse(Company company, Pageable pageable);

    @Query("SELECT e FROM WebEvent e WHERE e.company = :company " +
           "AND e.published = true AND e.archived = false " +
           "AND e.endDate >= :now " +
           "ORDER BY e.startDate ASC")
    List<WebEvent> findUpcomingEvents(@Param("company") Company company, @Param("now") LocalDateTime now);

    @Query("SELECT e FROM WebEvent e WHERE e.company = :company " +
           "AND e.published = true AND e.archived = false " +
           "AND e.endDate < :now " +
           "ORDER BY e.endDate DESC")
    List<WebEvent> findPastEvents(@Param("company") Company company, @Param("now") LocalDateTime now);
}

