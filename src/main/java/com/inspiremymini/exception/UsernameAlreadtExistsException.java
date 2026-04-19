package com.inspiremymini.exception;

import org.springframework.http.HttpStatus;

public class UsernameAlreadtExistsException extends AppException{
    public UsernameAlreadtExistsException(String username) {
        super("Username: " + username + " already exist.", HttpStatus.CONFLICT);
    }
}
