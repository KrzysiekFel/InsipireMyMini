package com.inspiremymini.exception.handler;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ErrorResponse(
        HttpStatus httpStatus,
        String message,
        LocalDateTime timestamp
) {}
