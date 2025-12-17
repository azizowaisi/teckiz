package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.WebAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebAlbumRepository extends JpaRepository<WebAlbum, Long> {

    Optional<WebAlbum> findByAlbumKey(String albumKey);

    Optional<WebAlbum> findBySlug(String slug);

    List<WebAlbum> findByCompany(Company company);

    List<WebAlbum> findByCompanyAndPublishedTrueAndArchivedFalse(Company company);

    List<WebAlbum> findByCompanyAndCarousalTrueAndPublishedTrue(Company company);
}

