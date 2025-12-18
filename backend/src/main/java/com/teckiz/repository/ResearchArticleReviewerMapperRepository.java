package com.teckiz.repository;

import com.teckiz.entity.ResearchArticle;
import com.teckiz.entity.ResearchArticleReviewerMapper;
import com.teckiz.entity.UserCompanyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResearchArticleReviewerMapperRepository extends JpaRepository<ResearchArticleReviewerMapper, Long> {

    Optional<ResearchArticleReviewerMapper> findByMapperKey(String mapperKey);

    List<ResearchArticleReviewerMapper> findByArticle(ResearchArticle article);

    List<ResearchArticleReviewerMapper> findByReviewer(UserCompanyRole reviewer);

    List<ResearchArticleReviewerMapper> findByArticleAndStatus(ResearchArticle article, String status);

    List<ResearchArticleReviewerMapper> findByReviewerAndStatus(UserCompanyRole reviewer, String status);
}

