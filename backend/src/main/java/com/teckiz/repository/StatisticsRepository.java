package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Statistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, Long> {

    List<Statistics> findByCompany(Company company);

    Page<Statistics> findByCompany(Company company, Pageable pageable);

    List<Statistics> findByCompanyAndStatType(Company company, String statType);

    List<Statistics> findByCompanyAndEntityTypeAndEntityId(Company company, String entityType, Long entityId);

    List<Statistics> findByCompanyAndRecordedAtBetween(Company company, LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM Statistics s WHERE s.company = :company AND s.statType = :statType AND s.recordedAt BETWEEN :start AND :end")
    List<Statistics> findByCompanyAndStatTypeAndRecordedAtBetween(
            @Param("company") Company company,
            @Param("statType") String statType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT s.entityType, s.entityId, COUNT(s) as count FROM Statistics s WHERE s.company = :company AND s.statType = :statType AND s.recordedAt BETWEEN :start AND :end GROUP BY s.entityType, s.entityId")
    List<Object[]> findTopEntitiesByStatType(
            @Param("company") Company company,
            @Param("statType") String statType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    Long countByCompanyAndStatType(Company company, String statType);

    Long countByCompanyAndStatTypeAndRecordedAtBetween(Company company, String statType, LocalDateTime start, LocalDateTime end);
}

