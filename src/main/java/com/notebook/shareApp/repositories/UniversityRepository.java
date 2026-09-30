package com.notebook.shareApp.repositories;
import com.notebook.shareApp.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UniversityRepository extends JpaRepository<University, Long> {

    Optional<University> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
