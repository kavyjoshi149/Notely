package com.notebook.shareApp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Placeholder home page; becomes the notes feed in Step 7.
    @GetMapping("/")
    public String home() {
        return "home";
    }
}

