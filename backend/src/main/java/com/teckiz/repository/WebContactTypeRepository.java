package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.WebContactType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebContactTypeRepository extends JpaRepository<WebContactType, Long> {

    Optional<WebContactType> findByTypeKey(String typeKey);

    List<WebContactType> findByCompany(Company company);
}

