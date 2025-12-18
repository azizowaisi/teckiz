package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.WebContacts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebContactsRepository extends JpaRepository<WebContacts, Long> {

    Optional<WebContacts> findByContactKey(String contactKey);

    List<WebContacts> findByCompany(Company company);

    Page<WebContacts> findByCompanyAndArchivedFalseOrderByPositionDesc(Company company, Pageable pageable);
}

