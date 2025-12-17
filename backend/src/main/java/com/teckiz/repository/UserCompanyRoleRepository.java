package com.teckiz.repository;

import com.teckiz.entity.Company;
import com.teckiz.entity.User;
import com.teckiz.entity.UserCompanyRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCompanyRoleRepository extends JpaRepository<UserCompanyRole, Long> {

    Optional<UserCompanyRole> findByCompanyAndUser(Company company, User user);

    Optional<UserCompanyRole> findByUserAndActiveTrue(User user);

    Optional<UserCompanyRole> findByCompanyAndUserAndActiveTrue(Company company, User user);

    Optional<UserCompanyRole> findByUserCompanyRoleKey(String userCompanyRoleKey);

    @Query("SELECT ucr FROM UserCompanyRole ucr " +
           "JOIN ucr.user u " +
           "WHERE ucr.company = :company " +
           "AND (:searchKey IS NULL OR :searchKey = '' OR " +
           "     LOWER(u.name) LIKE LOWER(CONCAT('%', :searchKey, '%')) OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :searchKey, '%'))) " +
           "ORDER BY u.email ASC")
    List<UserCompanyRole> findCompanyUsers(
            @Param("company") Company company,
            @Param("searchKey") String searchKey
    );

    @Query("SELECT ucr FROM UserCompanyRole ucr " +
           "JOIN ucr.user u " +
           "WHERE ucr.company = :company " +
           "AND (:searchKey IS NULL OR :searchKey = '' OR " +
           "     LOWER(u.name) LIKE LOWER(CONCAT('%', :searchKey, '%')) OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :searchKey, '%'))) " +
           "ORDER BY u.email ASC")
    List<UserCompanyRole> findCompanyUsersWithPagination(
            @Param("company") Company company,
            @Param("searchKey") String searchKey,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
}

