package com.notebook.shareApp.util;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Thrown by services for API errors; the advice turns it into {"message": "..."} with this status. */
@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
