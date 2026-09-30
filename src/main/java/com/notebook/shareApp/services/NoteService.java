package com.notebook.shareApp.services;

import com.notebook.shareApp.dto.NoteResponse;
import com.notebook.shareApp.dto.PageResponse;
import com.notebook.shareApp.dto.StatsResponse;
import com.notebook.shareApp.entity.*;
import com.notebook.shareApp.repositories.*;
import com.notebook.shareApp.util.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class NoteService {

    private static final long MAX_BYTES = 15L * 1024 * 1024;   // matches the "up to 15 MB" text on the upload page

    private final NoteRepository noteRepository;
    private final SubjectRepository subjectRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final FileStorageService storage;

    @Transactional(readOnly = true)
    public PageResponse<NoteResponse> search(String q, Long branchId, Long universityId, Integer semester,
                                             String sort, User viewer, int page, int size) {
        Long viewerUni = viewerUniversityId(viewer);
        Sort order = "downloads".equals(sort)
                ? Sort.by(Sort.Direction.DESC, "downloadCount").and(Sort.by(Sort.Direction.DESC, "createdAt"))
                : Sort.by(Sort.Direction.DESC, "createdAt");
        Page<Note> result = noteRepository.search(q == null ? "" : q.trim(), universityId, branchId, semester,
                viewerUni, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), order));
        return toPage(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<NoteResponse> mine(User viewer, int page, int size) {
        return toPage(noteRepository.findByUploaderIdOrderByCreatedAtDesc(viewer.getId(),
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))));
    }

    @Transactional(readOnly = true)
    public NoteResponse get(Long id, User viewer) {
        return toResponse(findVisible(id, viewer));
    }

    /** Returns the note for a download and bumps its counter. Caller must already be logged in. */
    @Transactional
    public Note prepareDownload(Long id, User viewer) {
        Note note = findVisible(id, viewer);
        noteRepository.incrementDownloads(id);
        return note;
    }

    @Transactional
    public NoteResponse create(User uploader, String title, String description, Long branchId,
                               String subjectName, String subjectCode, String semesterText,
                               boolean universityOnly, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please attach a PDF file");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "File is larger than 15 MB");
        }
        if (!looksLikePdf(file)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed");
        }
        if (title == null || title.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        if (subjectCode == null || subjectCode.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Subject code is required");
        }
        if (uploader.getUniversity() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Your profile has no university");
        }
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Select a valid branch"));

        Integer semester = null;
        if (semesterText != null && !semesterText.isBlank()) {
            try {
                semester = Integer.valueOf(semesterText.trim());
            } catch (NumberFormatException e) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Semester must be a number");
            }
        }

        String code = subjectCode.trim().toUpperCase();
        Integer sem = semester;
        Subject subject = subjectRepository
                .findByUniversityIdAndCodeIgnoreCase(uploader.getUniversity().getId(), code)
                .orElseGet(() -> {
                    Subject s = new Subject();
                    s.setCode(code);
                    s.setName(subjectName == null || subjectName.isBlank() ? code : subjectName.trim());
                    s.setSemester(sem);
                    s.setUniversity(uploader.getUniversity());
                    s.setBranch(branch);
                    return subjectRepository.save(s);
                });

        String stored;
        try {
            stored = storage.storePdf(file);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save the file, please try again");
        }

        Note note = new Note();
        note.setTitle(title.trim());
        note.setDescription(description == null ? null : description.trim());
        note.setFilePath(stored);
        note.setOriginalFileName(file.getOriginalFilename());
        note.setFileSize(file.getSize());
        note.setVisibility(universityOnly ? Visibility.UNIVERSITY_ONLY : Visibility.PUBLIC);
        note.setSubject(subject);
        note.setUploader(uploader);
        Note saved = noteRepository.save(note);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        return new StatsResponse(noteRepository.count(), userRepository.count(), noteRepository.totalDownloads());
    }

    // ---- helpers ----

    private Note findVisible(Long id, User viewer) {
        Note note = noteRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Note not found"));
        if (note.getVisibility() == Visibility.UNIVERSITY_ONLY) {
            Long viewerUni = viewerUniversityId(viewer);
            boolean isOwner = viewer != null && note.getUploader().getId().equals(viewer.getId());
            if (!isOwner && !note.getSubject().getUniversity().getId().equals(viewerUni)) {
                // 404 rather than 403 so private notes don't leak their existence
                throw new ApiException(HttpStatus.NOT_FOUND, "Note not found");
            }
        }
        return note;
    }

    private Long viewerUniversityId(User viewer) {
        return viewer != null && viewer.getUniversity() != null ? viewer.getUniversity().getId() : -1L;
    }

    private boolean looksLikePdf(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] head = in.readNBytes(5);
            return head.length == 5 && new String(head, java.nio.charset.StandardCharsets.ISO_8859_1).equals("%PDF-");
        } catch (IOException e) {
            return false;
        }
    }

    private PageResponse<NoteResponse> toPage(Page<Note> page) {
        return new PageResponse<>(page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(), page.getTotalPages(), page.getTotalElements());
    }

    private NoteResponse toResponse(Note n) {
        Subject s = n.getSubject();
        User u = n.getUploader();
        long days = n.getCreatedAt() == null ? 0 : ChronoUnit.DAYS.between(n.getCreatedAt(), LocalDateTime.now());
        return new NoteResponse(n.getId(), n.getTitle(), n.getDescription(), s.getName(), s.getCode(),
                s.getSemester(), s.getBranch().getName(), s.getUniversity().getName(), u.getName(),
                u.getUsername(), initials(u.getName()), n.getFileSize(), n.getDownloadCount(),
                n.getLikeCount(), Math.max(days, 0), n.getVisibility().name(), n.getAcademicYear());
    }

    public static String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String second = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + second).toUpperCase();
    }
}
