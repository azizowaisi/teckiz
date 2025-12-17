package com.teckiz.repository;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.PasswordSecrecy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordSecrecyRepository extends JpaRepository<PasswordSecrecy, Long> {

    Optional<PasswordSecrecy> findBySecretKey(String secretKey);

    List<PasswordSecrecy> findByCompanyModuleMapper(CompanyModuleMapper companyModuleMapper);

    Optional<PasswordSecrecy> findByCompanyModuleMapperAndSecretKey(CompanyModuleMapper companyModuleMapper, String secretKey);

    Optional<PasswordSecrecy> findByCompanyModuleMapperAndSecretKeyAndEmail(
            CompanyModuleMapper companyModuleMapper, String secretKey, String email);
}

