package com.inspiremymini.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends AppException{
    public EmailAlreadyExistsException(String email) {
        super("User with email: " + email + " already exist.", HttpStatus.CONFLICT);
    }
}
