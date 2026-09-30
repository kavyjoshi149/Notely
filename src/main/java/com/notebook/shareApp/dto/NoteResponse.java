package com.notebook.shareApp.dto;

public record NoteResponse(Long id, String title, String description, String subjectName, String code,
                           Integer semester, String branch, String university, String uploader,
                           String uploaderUsername, String initials, Long fileSize, int downloads,
                           int likes, long daysAgo, String visibility, String academicYear) {
}
