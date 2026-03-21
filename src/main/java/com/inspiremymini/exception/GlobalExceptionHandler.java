package com.inspiremymini.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException businessException) {
        return ResponseEntity.status(businessException.getHttpStatus())
                .body(new ErrorResponse(
                        businessException.getHttpStatus().value(),
                        businessException.getMessage(),
                        LocalDateTime.now()));
    }
}
