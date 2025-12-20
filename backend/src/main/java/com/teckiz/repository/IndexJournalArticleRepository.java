package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.IndexJournal;
import com.teckiz.entity.IndexJournalArticle;
import com.teckiz.entity.IndexJournalVolume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexJournalArticleRepository extends JpaRepository<IndexJournalArticle, Long> {

    Optional<IndexJournalArticle> findByArticleKey(String articleKey);

    Optional<IndexJournalArticle> findBySlug(String slug);

    List<IndexJournalArticle> findByCompany(Company company);

    List<IndexJournalArticle> findByIndexJournal(IndexJournal indexJournal);

    List<IndexJournalArticle> findByIndexJournalVolume(IndexJournalVolume volume);

    Page<IndexJournalArticle> findByCompanyAndPublishedTrueAndArchivedFalse(Company company, Pageable pageable);

    List<IndexJournalArticle> findByCompanyAndIndexJournalAndPublishedTrueAndArchivedFalse(
            Company company, IndexJournal indexJournal);

    Page<IndexJournalArticle> findByCompanyAndIndexJournalAndPublishedTrueAndArchivedFalse(
            Company company, IndexJournal indexJournal, Pageable pageable);

    List<IndexJournalArticle> findByCompanyAndIndexJournalVolumeAndPublishedTrueAndArchivedFalse(
            Company company, IndexJournalVolume volume);

    Page<IndexJournalArticle> findByCompanyAndIndexJournalVolumeAndPublishedTrueAndArchivedFalse(
            Company company, IndexJournalVolume volume, Pageable pageable);
}

