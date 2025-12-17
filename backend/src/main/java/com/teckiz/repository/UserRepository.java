package com.teckiz.repository;

import com.teckiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.isSuperAdmin = true")
    List<User> findSuperAdminList();

    List<User> findByIsEnabledTrue();

    Optional<User> findOneByEmail(String email);
}

