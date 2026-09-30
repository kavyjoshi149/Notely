package com.notebook.shareApp.repositories;

import com.notebook.shareApp.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    // used for the course -> branch dropdown
    List<Branch> findByCourseId(Long courseId);

    Optional<Branch> findByCourseIdAndNameIgnoreCase(Long courseId, String name);
}
