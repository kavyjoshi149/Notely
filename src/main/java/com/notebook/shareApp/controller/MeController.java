package com.notebook.shareApp.controller;

import com.notebook.shareApp.entity.User;
import com.notebook.shareApp.repositories.UserRepository;
import com.notebook.shareApp.security.CustomUserDetails;
import com.notebook.shareApp.services.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;

    @GetMapping("/api/me")
    public Map<String, Object> me(
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (principal == null) {
            return Map.of("authenticated", false);
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("authenticated", true);
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("username", user.getUsername());
        response.put("initials", NoteService.initials(user.getName()));
        response.put("batch", user.getBatch());
        response.put("university",
                user.getUniversity() == null ? null : user.getUniversity().getName());
        response.put("branch",
                user.getBranch() == null ? null : user.getBranch().getName());

        return response;
    }
}