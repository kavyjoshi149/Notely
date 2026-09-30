package com.notebook.shareApp.controller;

import com.notebook.shareApp.payload.RegisterRequest;
import com.notebook.shareApp.services.AcademicService;
import com.notebook.shareApp.services.NoteService;
import com.notebook.shareApp.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicApiController {

    private final AcademicService academicService;
    private final NoteService noteService;
    private final UserService userService;

    @GetMapping("/stats")
    public Object stats() {
        return noteService.stats();
    }

    @GetMapping("/universities")
    public Object universities() {
        return academicService.getUniversities();
    }

    @GetMapping("/courses")
    public Object courses() {
        return academicService.getCourses();
    }

    @GetMapping("/branches")
    public Object branches(@RequestParam Long courseId) {
        return academicService.getBranchesByCourse(courseId);
    }

    @GetMapping("/branches/all")
    public Object allBranches() {
        return academicService.getAllBranches();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request,
            BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Please check the registration fields."));
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Passwords do not match."));
        }

        var user = userService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", user.getId(), "username", user.getUsername()));
    }
}