package com.notebook.shareApp.controller;
import com.notebook.shareApp.payload.OptionResponse;
import com.notebook.shareApp.services.AcademicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Open endpoints (see SecurityConfig: /api/public/*) used by the register page dropdowns. */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicApiController {

    private final AcademicService academicService;

    @GetMapping("/branches")
    public List<OptionResponse> branches(@RequestParam Long courseId) {
        return academicService.getBranchesByCourse(courseId);
    }
}

