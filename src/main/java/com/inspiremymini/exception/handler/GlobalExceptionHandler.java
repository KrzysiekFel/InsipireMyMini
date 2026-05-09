package com.inspiremymini.exception.handler;

import com.inspiremymini.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(AppException appException) {
        return ResponseEntity.status(appException.getHttpStatus())
                .body(new ErrorResponse(
                        HttpStatus.valueOf(appException.getHttpStatus().value()),
                        appException.getMessage(),
                        LocalDateTime.now()));
    }
}
