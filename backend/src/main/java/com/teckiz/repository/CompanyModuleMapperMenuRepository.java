package com.teckiz.repository;

import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.CompanyModuleMapperMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyModuleMapperMenuRepository extends JpaRepository<CompanyModuleMapperMenu, Long> {

    Optional<CompanyModuleMapperMenu> findByMenuKey(String menuKey);

    Optional<CompanyModuleMapperMenu> findByCompanyModuleMapperAndMenuType(
            CompanyModuleMapper companyModuleMapper, String menuType);

    Optional<CompanyModuleMapperMenu> findByCompanyModuleMapperAndHomePageTrue(
            CompanyModuleMapper companyModuleMapper);

    List<CompanyModuleMapperMenu> findByCompanyModuleMapperAndPublicMenuTrueOrderByPositionAsc(
            CompanyModuleMapper companyModuleMapper);

    List<CompanyModuleMapperMenu> findByCompanyModuleMapperOrderByPositionAsc(
            CompanyModuleMapper companyModuleMapper);

    List<CompanyModuleMapperMenu> findByCompanyModuleMapperAndAvailableInMainMenuTrueAndPublicMenuTrueOrderByPositionAsc(
            CompanyModuleMapper companyModuleMapper);

    List<CompanyModuleMapperMenu> findByCompanyModuleMapperAndPublicMenuTrueAndMasterFalseAndAvailableInFooterMenuTrueOrderByPositionAsc(
            CompanyModuleMapper companyModuleMapper);

    List<CompanyModuleMapperMenu> findByCompanyModuleMapperAndPublicMenuTrueAndMasterFalseAndAvailableInHomePageTrueOrderByPositionAsc(
            CompanyModuleMapper companyModuleMapper);
}

