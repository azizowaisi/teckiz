package com.teckiz.repository;

import com.teckiz.entity.ResearchArticle;
import com.teckiz.entity.ResearchArticleAuthorMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResearchArticleAuthorMapperRepository extends JpaRepository<ResearchArticleAuthorMapper, Long> {

    Optional<ResearchArticleAuthorMapper> findByAuthorMapperKey(String authorMapperKey);

    List<ResearchArticleAuthorMapper> findByResearchArticle(ResearchArticle article);

    List<ResearchArticleAuthorMapper> findByResearchArticleAndArchivedFalse(ResearchArticle article);
}

