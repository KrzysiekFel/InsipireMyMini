package com.inspiremymini.exception.handler;

import com.inspiremymini.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // TODO: We need to add other exceptions
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(AppException appException) {
//        log.error("User creatioon failed, user alkready exists...");
        return ResponseEntity.status(appException.getHttpStatus())
                .body(new ErrorResponse(
                        HttpStatus.valueOf(appException.getHttpStatus().value()),
                        appException.getMessage(),
                        LocalDateTime.now()));
    }
}
