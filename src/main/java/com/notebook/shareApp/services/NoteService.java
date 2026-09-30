package com.notebook.shareApp.services;


import com.notebook.shareApp.repositories.*;
import com.notebook.shareApp.entity.*;
import com.notebook.shareApp.dto.*;
import com.notebook.shareApp.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final SubjectService subjectService;
    private final FileStorageService fileStorageService;

    @Transactional
    public Note upload(MultipartFile file, NoteUploadRequest req, Long uploaderId) {
        User user = userRepository.findById(uploaderId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Subject subject = subjectService.findOrCreateSubject(user, req);
        // validates the file and saves it to disk; throws InvalidFileException on failure,
        // which is caught in the controller so the form can be redisplayed with an error
        String storedName = fileStorageService.store(file);

        Note note = new Note();
        note.setTitle(req.getTitle().trim());
        note.setDescription(req.getDescription());
        note.setFilePath(storedName);
        note.setOriginalFileName(file.getOriginalFilename());
        note.setFileSize(file.getSize());
        note.setAcademicYear(req.getAcademicYear());
        note.setVisibility(req.isUniversityOnly() ? Visibility.UNIVERSITY_ONLY : Visibility.PUBLIC);
        note.setSubject(subject);
        note.setUploader(user);

        return noteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public Page<Note> getFeed(Pageable pageable) {
        return noteRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Note getById(Long id) {
        return noteRepository.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("This note doesn't exist or was removed."));
    }

    /** Loads the file for download, but only after checking visibility, and counts the download. */
    @Transactional
    public Resource downloadAndCount(Long id, User requester) {
        Note note = noteRepository.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("This note doesn't exist or was removed."));
        checkVisible(note, requester);
        note.setDownloadCount(note.getDownloadCount() + 1);
        return fileStorageService.load(note.getFilePath());
    }

    @Transactional
    public void delete(Long id, Long requesterId, boolean requesterIsAdmin) {
        Note note = getById(id);
        if (!note.getUploader().getId().equals(requesterId) && !requesterIsAdmin) {
            throw new AccessDeniedException("You can only delete your own notes.");
        }
        fileStorageService.delete(note.getFilePath());
        noteRepository.delete(note);
    }

    private void checkVisible(Note note, User requester) {

        if (note.getVisibility() != Visibility.UNIVERSITY_ONLY) {
            return;
        }
        boolean isOwner = requester != null
                && note.getUploader().getId().equals(requester.getId());

        boolean sameUniversity = requester != null
                && requester.getUniversity() != null
                && requester.getUniversity().getId()
                .equals(note.getSubject().getUniversity().getId());
        if (!isOwner && !sameUniversity) {
            throw new AccessDeniedException(
                    "This note is only visible to students at " + note.getSubject().getUniversity().getName() + ".");
        }
    }

    public static String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String second = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + second).toUpperCase();
    }
    @Transactional(readOnly = true)
    public StatsResponse stats() {
        return new StatsResponse(noteRepository.count(), userRepository.count(), noteRepository.totalDownloads());
    }

    @Transactional(readOnly = true)
    public Page<Note> getMine(Long uploaderId, Pageable pageable) {
        return noteRepository.findByUploaderIdOrderByCreatedAtDesc(uploaderId, pageable);
    }

    @Transactional(readOnly = true)
    public Note getVisibleById(Long id, User requester) {
        Note note = getById(id);
        checkVisible(note, requester);
        return note;
    }

}