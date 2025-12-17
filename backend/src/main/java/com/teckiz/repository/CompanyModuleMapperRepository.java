package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.CompanyModuleMapper;
import com.teckiz.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyModuleMapperRepository extends JpaRepository<CompanyModuleMapper, Long> {

    List<CompanyModuleMapper> findByCompany(Company company);

    Optional<CompanyModuleMapper> findByCompanyAndModuleAndArchivedFalse(Company company, Module module);

    Optional<CompanyModuleMapper> findByModuleMapperKeyAndArchivedFalseAndLiveTrue(String moduleMapperKey);

    Optional<CompanyModuleMapper> findByHostAndArchivedFalseAndLiveTrue(String host);

    @Query("SELECT cmm FROM CompanyModuleMapper cmm " +
           "JOIN cmm.module m " +
           "WHERE cmm.company = :company " +
           "AND cmm.archived = false " +
           "AND m.type = :type")
    Optional<CompanyModuleMapper> findByCompanyAndModuleType(@Param("company") Company company, @Param("type") String type);

    @Query("SELECT cmm FROM CompanyModuleMapper cmm " +
           "JOIN cmm.module m " +
           "WHERE cmm.company = :company " +
           "AND m.type = :type")
    Optional<CompanyModuleMapper> findByCompanyAndModuleTypeIgnoreArchive(@Param("company") Company company, @Param("type") String type);

    List<CompanyModuleMapper> findByModuleAndArchivedFalse(Module module);

    Optional<CompanyModuleMapper> findByModuleMapperKeyAndArchivedFalse(String moduleMapperKey);
}

