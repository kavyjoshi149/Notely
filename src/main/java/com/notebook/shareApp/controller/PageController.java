package com.notebook.shareApp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the Thymeleaf pages. All data is loaded by the page JS from the /api endpoints. */
@Controller
public class PageController {

    @GetMapping("/")
    public String home() { return "index"; }

    @GetMapping("/explore")
    public String explore() { return "explore"; }

    @GetMapping("/upload")
    public String upload() { return "upload"; }

    @GetMapping("/library")
    public String library() { return "library"; }

    @GetMapping("/notes/{id}")
    public String noteDetail(@org.springframework.web.bind.annotation.PathVariable Long id) { return "note-detail"; }
}
