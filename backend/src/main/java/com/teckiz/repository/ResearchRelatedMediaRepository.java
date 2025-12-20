package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.ResearchArticle;
import com.teckiz.entity.ResearchJournal;
import com.teckiz.entity.ResearchJournalVolume;
import com.teckiz.entity.ResearchRelatedMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResearchRelatedMediaRepository extends JpaRepository<ResearchRelatedMedia, Long> {

    Optional<ResearchRelatedMedia> findByRelatedMediaKey(String relatedMediaKey);

    List<ResearchRelatedMedia> findByCompany(Company company);

    List<ResearchRelatedMedia> findByResearchArticle(ResearchArticle researchArticle);

    List<ResearchRelatedMedia> findByResearchJournal(ResearchJournal researchJournal);

    List<ResearchRelatedMedia> findByResearchJournalVolume(ResearchJournalVolume volume);

    List<ResearchRelatedMedia> findByCompanyAndResearchArticle(Company company, ResearchArticle researchArticle);

    List<ResearchRelatedMedia> findByCompanyAndPosterTrue(Company company);
}

