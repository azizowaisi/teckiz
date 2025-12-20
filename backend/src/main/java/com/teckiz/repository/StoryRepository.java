package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Story;
import com.teckiz.entity.StoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    Optional<Story> findByStoryKey(String storyKey);

    List<Story> findByCompany(Company company);

    Page<Story> findByCompany(Company company, Pageable pageable);

    List<Story> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);

    List<Story> findByCompanyAndPublishedTrueAndArchivedFalse(Company company);

    Page<Story> findByCompanyAndPublishedTrueAndArchivedFalse(Company company, Pageable pageable);

    List<Story> findByCompanyAndStoryType(Company company, StoryType storyType);

    List<Story> findByCompanyAndStoryTypeAndPublishedTrueAndArchivedFalse(Company company, StoryType storyType);

    Page<Story> findByCompanyAndStoryTypeIdAndPublishedTrueAndArchivedFalse(Company company, Long storyTypeId, Pageable pageable);
}

