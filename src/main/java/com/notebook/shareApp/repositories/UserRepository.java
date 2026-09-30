package com.notebook.shareApp.repositories;

import com.notebook.shareApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"university", "course", "branch"})
    Optional<User> findWithAcademicByUsername(String username);

    // allows login with username or email
    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUniversityIdAndRollNumber(Long universityId, String rollNumber);
}
