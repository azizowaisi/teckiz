package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.ResearchJournal;
import com.teckiz.entity.ResearchJournalVolume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResearchJournalVolumeRepository extends JpaRepository<ResearchJournalVolume, Long> {

    Optional<ResearchJournalVolume> findByVolumeKey(String volumeKey);

    Optional<ResearchJournalVolume> findBySlug(String slug);

    List<ResearchJournalVolume> findByCompany(Company company);

    Page<ResearchJournalVolume> findByCompany(Company company, Pageable pageable);

    List<ResearchJournalVolume> findByResearchJournal(ResearchJournal journal);

    Page<ResearchJournalVolume> findByResearchJournal(ResearchJournal journal, Pageable pageable);

    List<ResearchJournalVolume> findByResearchJournalAndPublishedTrueAndArchivedFalse(ResearchJournal journal);
}

