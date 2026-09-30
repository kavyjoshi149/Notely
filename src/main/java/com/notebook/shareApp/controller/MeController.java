package com.notebook.shareApp.controller;

import com.notebook.shareApp.dto.MeResponse;
import com.notebook.shareApp.entity.User;
import com.notebook.shareApp.security.CurrentUser;
import com.notebook.shareApp.services.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lets every page ask "who is logged in?" so the header and library can render the right state. */
@RestController
@RequiredArgsConstructor
public class MeController {

    private final CurrentUser currentUser;

    @GetMapping("/api/me")
    @Transactional(readOnly = true)
    public MeResponse me(Authentication auth) {
        User u = currentUser.orNull(auth);
        if (u == null) return MeResponse.anonymous();
        return new MeResponse(true, u.getId(), u.getName(), u.getUsername(), NoteService.initials(u.getName()),
                u.getEmail(), u.getBatch(),
                u.getUniversity() == null ? null : u.getUniversity().getName(),
                u.getCourse() == null ? null : u.getCourse().getName(),
                u.getBranch() == null ? null : u.getBranch().getName());
    }
}
