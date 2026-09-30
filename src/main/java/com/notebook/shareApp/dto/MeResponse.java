package com.notebook.shareApp.dto;

public record MeResponse(boolean authenticated, Long id, String name, String username, String initials,
                         String email, String batch, String university, String course, String branch) {
    public static MeResponse anonymous() {
        return new MeResponse(false, null, null, null, null, null, null, null, null, null);
    }
}
