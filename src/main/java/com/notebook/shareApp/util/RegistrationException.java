package com.notebook.shareApp.util;

import lombok.Getter;

/** Thrown by UserService when a registration rule fails; 'field' is the form field to attach the error to. */
@Getter
public class RegistrationException extends RuntimeException {

    private final String field;

    public RegistrationException(String field, String message) {
        super(message);
        this.field = field;
    }
}
