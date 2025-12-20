package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.IndexJournal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexJournalRepository extends JpaRepository<IndexJournal, Long> {

    Optional<IndexJournal> findByJournalKey(String journalKey);

    Optional<IndexJournal> findBySlug(String slug);

    List<IndexJournal> findByCompany(Company company);

    List<IndexJournal> findByCompanyAndArchivedFalse(Company company);

    Page<IndexJournal> findByCompanyAndArchivedFalse(Company company, Pageable pageable);

    List<IndexJournal> findByCompanyAndActiveTrueAndArchivedFalse(Company company);

    List<IndexJournal> findByCompanyAndNameContaining(Company company, String name);
}

