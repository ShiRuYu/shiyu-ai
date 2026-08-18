package com.shiyu.ai.web.common;

import com.shiyu.ai.common.core.api.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

/** Keeps platform APIs on stable 404/409/403/422 semantics instead of leaking JVM exceptions. */
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> invalidArgument(IllegalArgumentException error) {
        String message = error.getMessage() == null ? "invalid request" : error.getMessage();
        String normalized = message.toLowerCase(Locale.ROOT);
        HttpStatus status = normalized.contains("not found") ? HttpStatus.NOT_FOUND
                : normalized.contains("access denied") || normalized.contains("forbidden") ? HttpStatus.FORBIDDEN
                : normalized.contains("already") || normalized.contains("conflict") || normalized.contains("modified") ? HttpStatus.CONFLICT
                : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status).body(Result.common(null, status.value(), message, message, false));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Result<Void>> invalidState(IllegalStateException error) {
        String message = error.getMessage() == null ? "request cannot be completed" : error.getMessage();
        HttpStatus status = message.toLowerCase(Locale.ROOT).contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(Result.common(null, status.value(), message, message, false));
    }
}
