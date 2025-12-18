package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.ResearchJournal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResearchJournalRepository extends JpaRepository<ResearchJournal, Long> {

    Optional<ResearchJournal> findByJournalKey(String journalKey);

    Optional<ResearchJournal> findBySlug(String slug);

    List<ResearchJournal> findByCompany(Company company);
}

