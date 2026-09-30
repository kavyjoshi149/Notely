package com.notebook.shareApp.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoteUploadRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150)
    private String title;

    @Size(max = 1000, message = "Description is too long")
    private String description;

    @NotBlank(message = "Subject code is required")
    @Size(max = 20)
    private String subjectCode;

    @NotBlank(message = "Subject name is required")
    @Size(max = 120)
    private String subjectName;

    @NotNull(message = "Select a branch")
    private Long branchId;

    private Integer semester;

    @Size(max = 20)
    private String academicYear;

    // maps to Visibility.UNIVERSITY_ONLY when true, PUBLIC otherwise
    private boolean universityOnly;
}