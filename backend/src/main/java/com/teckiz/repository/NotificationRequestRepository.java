package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.NotificationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRequestRepository extends JpaRepository<NotificationRequest, Long> {

    Optional<NotificationRequest> findByRequestKey(String requestKey);

    List<NotificationRequest> findByCompany(Company company);

    Page<NotificationRequest> findByCompany(Company company, Pageable pageable);

    List<NotificationRequest> findByCompanyAndStatus(Company company, String status);

    Page<NotificationRequest> findByCompanyAndStatus(Company company, String status, Pageable pageable);

    List<NotificationRequest> findByCompanyAndStatusAndScheduledForLessThanEqual(
            Company company, String status, LocalDateTime dateTime);

    List<NotificationRequest> findByCompanyAndTargetTypeAndTargetId(
            Company company, String targetType, Long targetId);

    List<NotificationRequest> findByCompanyAndTargetType(Company company, String targetType);

    List<NotificationRequest> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);
}

