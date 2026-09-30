package com.notebook.shareApp.payload;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name is too long")
    private String name;

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[A-Za-z0-9_]{3,30}$",
            message = "3-30 characters: letters, numbers and underscore only")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @NotBlank(message = "Batch is required")
    @Size(max = 20)
    private String batch;          // e.g. 2022-2026

    @NotBlank(message = "Roll number is required")
    @Size(max = 30)
    private String rollNumber;

    @NotNull(message = "Select your university")
    private Long universityId;

    @NotNull(message = "Select your course")
    private Long courseId;

    @NotNull(message = "Select your branch")
    private Long branchId;
}
