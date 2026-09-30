package com.notebook.shareApp.security;

import com.notebook.shareApp.entity.User;
import com.notebook.shareApp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** Resolves the logged-in user (with university/course/branch loaded) from the Spring Security context. */
@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final UserRepository userRepository;

    /** Returns null for anonymous visitors. */
    public User orNull(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userRepository.findWithAcademicByUsername(auth.getName()).orElse(null);
    }
}
