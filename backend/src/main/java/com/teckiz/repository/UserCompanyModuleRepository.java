package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.User;
import com.teckiz.entity.UserCompanyModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCompanyModuleRepository extends JpaRepository<UserCompanyModule, Long> {

    List<UserCompanyModule> findByUserAndCompany(User user, Company company);

    Optional<UserCompanyModule> findByUserCompanyModuleKey(String userCompanyModuleKey);
}

