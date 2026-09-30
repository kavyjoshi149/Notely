package com.notebook.shareApp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Login is handled by Spring Security (POST /login, form fields "username" and "password").
 * Registration is a JSON call to POST /api/public/register (see PublicApiController).
 * Both pages share the same template; auth.js opens the right tab from the URL.
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() { return "auth"; }

    @GetMapping("/register")
    public String registerPage() { return "auth"; }
}
