package com.inspiremymini.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        int httpStatus,
        String message,
        LocalDateTime timestamp
) {}
