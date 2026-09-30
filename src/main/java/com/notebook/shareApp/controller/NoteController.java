package com.notebook.shareApp.controller;


import com.notebook.shareApp.entity.*;
import com.notebook.shareApp.security.CustomUserDetails;
import com.notebook.shareApp.services.*;
import com.notebook.shareApp.repositories.*;
import com.notebook.shareApp.dto.*;
import com.notebook.shareApp.util.InvalidFileException;
import com.notebook.shareApp.util.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final UserRepository userRepository;
    private final AcademicService academicService;

    @GetMapping("/upload")
    public String uploadForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        User user = currentUser(principal);
        model.addAttribute("noteUploadRequest", new NoteUploadRequest());
        model.addAttribute("branches", academicService.getBranchesByCourse(user.getCourse().getId()));
        return "note/upload";
    }

    @PostMapping("/upload")
    public String upload(@AuthenticationPrincipal CustomUserDetails principal,
                         @Valid @ModelAttribute("noteUploadRequest") NoteUploadRequest request,
                         BindingResult result,
                         @RequestParam("file") MultipartFile file,
                         Model model) {

        if (!result.hasErrors()) {
            try {
                Note note = noteService.upload(file, request, principal.getId());
                return "redirect:/notes/" + note.getId();
            } catch (InvalidFileException e) {
                result.reject("file", e.getMessage());
            }
        }

        User user = currentUser(principal);
        model.addAttribute("branches", academicService.getBranchesByCourse(user.getCourse().getId()));
        return "note/upload";
    }
//
//    @GetMapping("/{id}")
//    public String detail(@PathVariable Long id,
//                         @AuthenticationPrincipal CustomUserDetails principal,
//                         Model model) {
//        Note note = noteService.getById(id);
//        model.addAttribute("note", note);
//        model.addAttribute("isOwner", note.getUploader().getId().equals(principal.getId()));
//        return "note/detail";
//    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> download(@PathVariable Long id,
                                             @AuthenticationPrincipal CustomUserDetails principal) {
        User requester = currentUser(principal);
        Note note = noteService.getById(id); // for the filename
        Resource resource = noteService.downloadAndCount(id, requester);

        String filename = note.getOriginalFileName() != null ? note.getOriginalFileName() : note.getFilePath();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        noteService.delete(id, principal.getId(), isAdmin);
        return "redirect:/";
    }

    private User currentUser(CustomUserDetails principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}