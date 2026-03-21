package com.inspiremymini.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException{

    public UserNotFoundException(Long id) {
        super("User with id: " + id + " not found ", HttpStatus.NOT_FOUND);
    }
}
