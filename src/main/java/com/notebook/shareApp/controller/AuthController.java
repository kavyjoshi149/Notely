package com.notebook.shareApp.controller;

import com.notebook.shareApp.payload.RegisterRequest;
import com.notebook.shareApp.services.AcademicService;
import com.notebook.shareApp.services.UserService;
import com.notebook.shareApp.util.RegistrationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AcademicService academicService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        loadDropdowns(model, null);
        return "auth";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                           BindingResult result,
                           Model model) {

        if (!result.hasFieldErrors("password") && !result.hasFieldErrors("confirmPassword")
                && !request.getPassword().equals(request.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }

        if (!result.hasErrors()) {
            try {
                userService.register(request);
                return "redirect:/login?registered";
            } catch (RegistrationException e) {
                result.rejectValue(e.getField(), "registration", e.getMessage());
            } catch (DataIntegrityViolationException e) {
                // two people registering the same username/email at the same instant
                result.reject("registration", "Username, email or roll number is already in use");
            }
        }

        loadDropdowns(model, request.getCourseId());
        return "auth/register";
    }

    private void loadDropdowns(Model model, Long courseId) {
        model.addAttribute("universities", academicService.getUniversities());
        model.addAttribute("courses", academicService.getCourses());
        model.addAttribute("branches", academicService.getBranchesByCourse(courseId));
    }
}
