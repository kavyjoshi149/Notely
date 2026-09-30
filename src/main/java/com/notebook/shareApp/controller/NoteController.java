package com.notebook.shareApp.controller;

import com.notebook.shareApp.dto.NoteResponse;
import com.notebook.shareApp.dto.PageResponse;
import com.notebook.shareApp.entity.Note;
import com.notebook.shareApp.entity.User;
import com.notebook.shareApp.security.CurrentUser;
import com.notebook.shareApp.services.FileStorageService;
import com.notebook.shareApp.services.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.notebook.shareApp.util.ApiException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** JSON API behind the Explore, Home, Library, Note detail and Upload pages. */
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final FileStorageService storage;
    private final CurrentUser currentUser;

    @GetMapping
    public PageResponse<NoteResponse> list(@RequestParam(defaultValue = "") String q,
                                           @RequestParam(required = false) Long branchId,
                                           @RequestParam(required = false) Long universityId,
                                           @RequestParam(required = false) Integer semester,
                                           @RequestParam(defaultValue = "recent") String sort,   // "recent" | "downloads"
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size,
                                           Authentication auth) {
        return noteService.search(q, branchId, universityId, semester, sort, currentUser.orNull(auth), page, size);
    }

    @GetMapping("/mine")
    public PageResponse<NoteResponse> mine(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size,
                                           Authentication auth) {
        return noteService.mine(requireUser(auth), page, size);
    }

    @GetMapping("/{id}")
    public NoteResponse get(@PathVariable Long id, Authentication auth) {
        return noteService.get(id, currentUser.orNull(auth));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id, Authentication auth) throws IOException {
        User viewer = requireUser(auth);
        Note note = noteService.prepareDownload(id, viewer);
        Resource file;
        try {
            file = storage.load(note.getFilePath());
        } catch (IOException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "The file for this note is missing on the server");
        }
        String name = note.getOriginalFileName() != null ? note.getOriginalFileName() : note.getTitle() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(name, StandardCharsets.UTF_8).build().toString())
                .body(file);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoteResponse> upload(@RequestParam("file") MultipartFile file,
                                               @RequestParam String title,
                                               @RequestParam(required = false) String description,
                                               @RequestParam Long branchId,
                                               @RequestParam(required = false) String subjectName,
                                               @RequestParam String subjectCode,
                                               @RequestParam(required = false) String semester,
                                               @RequestParam(defaultValue = "false") boolean universityOnly,
                                               Authentication auth) {
        User uploader = requireUser(auth);
        NoteResponse created = noteService.create(uploader, title, description, branchId, subjectName,
                subjectCode, semester, universityOnly, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    private User requireUser(Authentication auth) {
        User u = currentUser.orNull(auth);
        if (u == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "Please log in");
        return u;
    }
}
