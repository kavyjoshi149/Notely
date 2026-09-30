package com.notebook.shareApp.payload;
/** Small id + name pair used for dropdown options (and the branch JSON endpoint). */
public record OptionResponse(Long id, String name) {
}
