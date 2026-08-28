package com.shiyu.ai.web.common;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/** Keeps platform APIs on stable 404/409/403/422 semantics instead of leaking JVM exceptions. */
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> invalidArgument(IllegalArgumentException error) {
        String normalized = String.valueOf(error.getMessage()).toLowerCase(Locale.ROOT);
        HttpStatus status = normalized.contains("not found") ? HttpStatus.NOT_FOUND
                : normalized.contains("access denied") || normalized.contains("forbidden") ? HttpStatus.FORBIDDEN
                : normalized.contains("already") || normalized.contains("conflict") || normalized.contains("modified") ? HttpStatus.CONFLICT
                : HttpStatus.UNPROCESSABLE_CONTENT;
        return failure(status);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Result<Void>> invalidState(IllegalStateException error) {
        HttpStatus status = String.valueOf(error.getMessage()).toLowerCase(Locale.ROOT).contains("not found")
                ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        return failure(status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> unexpected(RuntimeException error) {
        return failure(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result<Void>> responseStatus(ResponseStatusException error) {
        HttpStatus status = HttpStatus.valueOf(error.getStatusCode().value());
        return failure(status);
    }

    private ResponseEntity<Result<Void>> failure(HttpStatus status) {
        BizResultCode code = switch (status) {
            case NOT_FOUND -> BizResultCode.NOT_FOUND;
            case FORBIDDEN -> BizResultCode.FORBIDDEN;
            case UNPROCESSABLE_CONTENT -> BizResultCode.ERR_10007;
            case CONFLICT -> BizResultCode.ERR_10008;
            default -> BizResultCode.ERROR;
        };
        return ResponseEntity.status(status).body(Result.common(null, code.getCode(), code.getMsg(), false));
    }
}
