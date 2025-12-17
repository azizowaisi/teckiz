package com.teckiz.repository;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.WebNewsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebNewsTypeRepository extends JpaRepository<WebNewsType, Long> {

    List<WebNewsType> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);

    Optional<WebNewsType> findByTypeKey(String typeKey);
}

