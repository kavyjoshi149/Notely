package com.notebook.shareApp.controller;
import com.notebook.shareApp.services.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final NoteService noteService;

    @GetMapping("/")
    public String home(@PageableDefault(size = 9, sort = "createdAt", direction = Sort.Direction.DESC)
                       Pageable pageable, Model model) {
        model.addAttribute("notesPage", noteService.getFeed(pageable));
        return "index";
    }
}

