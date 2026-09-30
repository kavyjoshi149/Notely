package com.notebook.shareApp.repositories;
import com.notebook.shareApp.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    // duplicate check before creating a subject
    Optional<Subject> findByUniversityIdAndCodeIgnoreCase(Long universityId, String code);

    List<Subject> findByUniversityIdAndBranchId(Long universityId, Long branchId);

    // searchable subject picker on the upload page
    @Query("""
            SELECT s FROM Subject s
            WHERE s.university.id = :universityId
              AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    List<Subject> searchInUniversity(@Param("universityId") Long universityId,
                                     @Param("keyword") String keyword);
}
