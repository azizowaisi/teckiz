package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.IndexJournal;
import com.teckiz.entity.IndexJournalVolume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexJournalVolumeRepository extends JpaRepository<IndexJournalVolume, Long> {

    Optional<IndexJournalVolume> findByVolumeKey(String volumeKey);

    Optional<IndexJournalVolume> findBySlug(String slug);

    List<IndexJournalVolume> findByCompany(Company company);

    List<IndexJournalVolume> findByIndexJournal(IndexJournal indexJournal);

    List<IndexJournalVolume> findByCompanyAndIndexJournalAndArchivedFalse(
            Company company, IndexJournal indexJournal);
    Page<IndexJournalVolume> findByCompanyAndIndexJournalAndArchivedFalse(
            Company company, IndexJournal indexJournal, Pageable pageable);

    Page<IndexJournalVolume> findByCompanyAndIndexJournalAndPublishedTrueAndArchivedFalse(
            Company company, IndexJournal indexJournal, Pageable pageable);

    List<IndexJournalVolume> findByCompanyAndIndexJournalAndPublishedTrueAndArchivedFalse(
            Company company, IndexJournal indexJournal);
}

