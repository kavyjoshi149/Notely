package com.notebook.shareApp.controller;


import com.notebook.shareApp.dto.NoteUploadRequest;
import com.notebook.shareApp.entity.Note;
import com.notebook.shareApp.entity.Subject;
import com.notebook.shareApp.entity.User;
import com.notebook.shareApp.repositories.UserRepository;
import com.notebook.shareApp.security.CustomUserDetails;
import com.notebook.shareApp.services.NoteService;
import com.notebook.shareApp.util.InvalidFileException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteApiController {

    private final NoteService noteService;
    private final UserRepository userRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @ModelAttribute NoteUploadRequest request,
            BindingResult result,
            @RequestParam("file") MultipartFile file) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        if (result.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Please complete all required fields."));
        }

        try {
            Note note = noteService.upload(file, request, principal.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("id", note.getId()));

        } catch (InvalidFileException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", exception.getMessage()));
        }
    }
    @GetMapping
    public Map<String, Object> listNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "") String q) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Page<Note> notes = noteService.getFeed(
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        String query = q.trim().toLowerCase(Locale.ROOT);

        List<Map<String, Object>> content = notes.getContent().stream()
                .filter(note -> matchesSearch(note, query))
                .map(this::toExploreResponse)
                .toList();

        return Map.of(
                "content", content,
                "page", notes.getNumber(),
                "totalPages", notes.getTotalPages(),
                "totalElements", notes.getTotalElements()
        );
    }

    private boolean matchesSearch(Note note, String query) {
        if (query.isBlank()) {
            return true;
        }

        Subject subject = note.getSubject();

        return note.getTitle().toLowerCase(Locale.ROOT).contains(query)
                || subject.getCode().toLowerCase(Locale.ROOT).contains(query)
                || subject.getName().toLowerCase(Locale.ROOT).contains(query)
                || note.getUploader().getName().toLowerCase(Locale.ROOT).contains(query);
    }

    private Map<String, Object> toExploreResponse(Note note) {
        Subject subject = note.getSubject();

        return Map.of(
                "id", note.getId(),
                "title", note.getTitle(),
                "description", note.getDescription() == null ? "" : note.getDescription(),
                "subjectName", subject.getName(),
                "code", subject.getCode(),
                "branch", subject.getBranch().getName(),
                "university", subject.getUniversity().getName(),
                "uploader", note.getUploader().getName(),
                "initials", NoteService.initials(note.getUploader().getName()),
                "downloads", note.getDownloadCount()
        );
    }

    @GetMapping("/mine")
    public ResponseEntity<?> myNotes(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Page<Note> notes = noteService.getMine(
                principal.getId(),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return ResponseEntity.ok(Map.of(
                "content", notes.getContent().stream()
                        .map(this::toExploreResponse)
                        .toList(),
                "page", notes.getNumber(),
                "totalPages", notes.getTotalPages(),
                "totalElements", notes.getTotalElements()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> noteDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        User viewer = principal == null ? null : userRepository.findById(principal.getId())
                .orElse(null);

        Note note = noteService.getVisibleById(id, viewer);
        return ResponseEntity.ok(toDetailResponse(note));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Note note = noteService.getById(id);
        Resource file = noteService.downloadAndCount(id, user);

        String filename = note.getOriginalFileName() == null
                ? note.getTitle() + ".pdf"
                : note.getOriginalFileName();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(filename, StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(file);
    }


    private Map<String, Object> toDetailResponse(Note note) {
        Subject subject = note.getSubject();

        long daysAgo = note.getCreatedAt() == null
                ? 0
                : Math.max(
                0,
                ChronoUnit.DAYS.between(
                        note.getCreatedAt(),
                        LocalDateTime.now()
                )
        );

        return Map.ofEntries(
                Map.entry("id", note.getId()),
                Map.entry("title", note.getTitle()),
                Map.entry(
                        "description",
                        note.getDescription() == null
                                ? ""
                                : note.getDescription()
                ),
                Map.entry("subjectName", subject.getName()),
                Map.entry("code", subject.getCode()),
                Map.entry(
                        "semester",
                        subject.getSemester() == null
                                ? ""
                                : subject.getSemester()
                ),
                Map.entry("branch", subject.getBranch().getName()),
                Map.entry("university", subject.getUniversity().getName()),
                Map.entry("uploader", note.getUploader().getName()),
                Map.entry(
                        "initials",
                        NoteService.initials(note.getUploader().getName())
                ),
                Map.entry(
                        "fileSize",
                        note.getFileSize() == null
                                ? 0
                                : note.getFileSize()
                ),
                Map.entry("downloads", note.getDownloadCount()),
                Map.entry("likes", note.getLikeCount()),
                Map.entry("daysAgo", daysAgo)
        );

    }

}