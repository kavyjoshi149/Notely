package com.notebook.shareApp.repositories;
import com.notebook.shareApp.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteRepository extends JpaRepository<Note, Long> {

    @EntityGraph(attributePaths = {"subject", "subject.university", "subject.branch", "uploader"})
    Page<Note> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"subject", "subject.university", "subject.branch", "uploader"})
    Page<Note> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId, Pageable pageable);

    @EntityGraph(attributePaths = {"subject", "subject.university", "subject.branch", "uploader"})
    Page<Note> findBySubjectIdOrderByCreatedAtDesc(Long subjectId, Pageable pageable);

    @EntityGraph(attributePaths = {"subject", "subject.university", "subject.branch", "uploader"})
    java.util.Optional<Note> findWithDetailsById(Long id);

    @Query("select coalesce(sum(n.downloadCount), 0) from Note n")
    long totalDownloads();

    @Modifying
    @Query("update Note n set n.downloadCount = n.downloadCount + 1 where n.id = :id")
    void incrementDownloads(@Param("id") Long id);

    // NOTE: pass keyword as "" (empty string) when there is no search text, never null.
    // The id/semester filters can be null. viewerUniversityId is the logged-in user's university
    // (use -1 for anonymous visitors): UNIVERSITY_ONLY notes are only returned for that university.
    // Ordering comes from the Pageable's Sort (see NoteService.search).
    @EntityGraph(attributePaths = {"subject", "subject.university", "subject.branch", "uploader"})
    @Query("""
            SELECT n FROM Note n JOIN n.subject s
            WHERE (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(n.uploader.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:universityId IS NULL OR s.university.id = :universityId)
              AND (:branchId IS NULL OR s.branch.id = :branchId)
              AND (:semester IS NULL OR s.semester = :semester)
              AND (n.visibility = com.notebook.shareApp.entity.Visibility.PUBLIC
                   OR s.university.id = :viewerUniversityId)
            """)
    Page<Note> search(@Param("keyword") String keyword,
                      @Param("universityId") Long universityId,
                      @Param("branchId") Long branchId,
                      @Param("semester") Integer semester,
                      @Param("viewerUniversityId") Long viewerUniversityId,
                      Pageable pageable);
}
