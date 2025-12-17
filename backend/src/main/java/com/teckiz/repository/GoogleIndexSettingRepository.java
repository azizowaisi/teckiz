package com.teckiz.repository;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.GoogleIndexSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoogleIndexSettingRepository extends JpaRepository<GoogleIndexSetting, Long> {

    Optional<GoogleIndexSetting> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);
}

