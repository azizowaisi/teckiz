package com.teckiz.repository;

import com.teckiz.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByArchivedFalse();

    Optional<Module> findByType(String type);

    Optional<Module> findByName(String name);

    Optional<Module> findByModuleKeyAndArchivedFalse(String moduleKey);
}

