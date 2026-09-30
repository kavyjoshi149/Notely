package com.notebook.shareApp.util;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Turns known exceptions into the shared error page instead of a stack trace.
 *  AccessDeniedException is NOT handled here — Spring Security's own filter
 *  catches it first and renders a 403, which is what we want. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NotFoundException e, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("message", e.getMessage());
        return "error/error";
    }

    @ExceptionHandler(InvalidFileException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidFile(InvalidFileException e, Model model) {
        model.addAttribute("status", 400);
        model.addAttribute("message", e.getMessage());
        return "error/error";
    }
}
