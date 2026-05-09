package com.inspiremymini.exception;

import org.springframework.http.HttpStatus;

public class UnauthenticatedException extends AppException{
    public UnauthenticatedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
