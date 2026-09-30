package com.notebook.shareApp.controller;

import com.notebook.shareApp.dto.BranchResponse;
import com.notebook.shareApp.dto.StatsResponse;
import com.notebook.shareApp.payload.OptionResponse;
import com.notebook.shareApp.payload.RegisterRequest;
import com.notebook.shareApp.repositories.BranchRepository;
import com.notebook.shareApp.services.AcademicService;
import com.notebook.shareApp.services.NoteService;
import com.notebook.shareApp.services.UserService;
import com.notebook.shareApp.util.ApiException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Open endpoints (see SecurityConfig: /api/public/**) used by the login/register, home and explore pages. */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicApiController {

    private final AcademicService academicService;
    private final UserService userService;
    private final NoteService noteService;
    private final BranchRepository branchRepository;

    @GetMapping("/universities")
    public List<OptionResponse> universities() {
        return academicService.getUniversities();
    }

    @GetMapping("/courses")
    public List<OptionResponse> courses() {
        return academicService.getCourses();
    }

    @GetMapping("/branches")
    public List<OptionResponse> branches(@RequestParam Long courseId) {
        return academicService.getBranchesByCourse(courseId);
    }

    /** Every branch with its course, used by the Explore filter chips. */
    @GetMapping("/branches/all")
    public List<BranchResponse> allBranches() {
        return branchRepository.findAll().stream()
                .map(b -> new BranchResponse(b.getId(), b.getName(), b.getCourse().getId(), b.getCourse().getName()))
                .toList();
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return noteService.stats();
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new com.notebook.shareApp.util.RegistrationException("confirmPassword", "Passwords do not match");
        }
        try {
            userService.register(request);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // two people registering the same username/email at the same instant
            throw new ApiException(HttpStatus.CONFLICT, "Username, email or roll number is already in use");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Account created"));
    }
}
