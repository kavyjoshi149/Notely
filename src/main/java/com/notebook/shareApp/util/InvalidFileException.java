package com.notebook.shareApp.util;

/** Thrown when an uploaded file fails validation (wrong type, too big, empty, unreadable). */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}