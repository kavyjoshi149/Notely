package com.notebook.shareApp.util;

/** Thrown when a requested entity (note, user, branch...) does not exist. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
