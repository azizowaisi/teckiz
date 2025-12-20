package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.Notification;
import com.teckiz.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByNotificationKey(String notificationKey);

    List<Notification> findByUser(User user);

    Page<Notification> findByUser(User user, Pageable pageable);

    List<Notification> findByUserAndReadFalse(User user);

    Page<Notification> findByUserAndReadFalse(User user, Pageable pageable);

    List<Notification> findByCompany(Company company);

    Page<Notification> findByCompany(Company company, Pageable pageable);

    List<Notification> findByCompanyAndReadFalse(Company company);

    Long countByUserAndReadFalse(User user);
}

