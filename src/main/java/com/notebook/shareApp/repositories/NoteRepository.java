package com.notebook.shareApp.repositories;
import com.notebook.shareApp.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteRepository extends JpaRepository<Note, Long> {

    @EntityGraph(attributePaths = {"subject", "subject.university", "subject.branch", "uploader"})
    Page<Note> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"subject", "subject.university", "subject.branch", "uploader"})
    Page<Note> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId, Pageable pageable);

    @EntityGraph(attributePaths = {"subject", "subject.university", "subject.branch", "uploader"})
    Page<Note> findBySubjectIdOrderByCreatedAtDesc(Long subjectId, Pageable pageable);

    // NOTE: pass keyword as "" (empty string) when there is no search text, never null.
    // The id/semester filters can be null.
    @EntityGraph(attributePaths = {"subject", "subject.university", "subject.branch", "uploader"})
    @Query("""
            SELECT n FROM Note n JOIN n.subject s
            WHERE (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:universityId IS NULL OR s.university.id = :universityId)
              AND (:branchId IS NULL OR s.branch.id = :branchId)
              AND (:semester IS NULL OR s.semester = :semester)
            ORDER BY n.createdAt DESC
            """)
    Page<Note> search(@Param("keyword") String keyword,
                      @Param("universityId") Long universityId,
                      @Param("branchId") Long branchId,
                      @Param("semester") Integer semester,
                      Pageable pageable);
}
