package com.shiyu.ai.web.common;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/**
 * 统一处理 Web 层异常并转换为稳定的 API 错误响应。
 */
@RestControllerAdvice
public class ApiExceptionHandler {
    /**
     * {@code invalidArgument} 执行当前类型定义的业务操作。
     *
     * @param error 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> invalidArgument(IllegalArgumentException error) {
        String normalized = String.valueOf(error.getMessage()).toLowerCase(Locale.ROOT);
        HttpStatus status =
                normalized.contains("not found")
                        ? HttpStatus.NOT_FOUND
                        : normalized.contains("access denied") || normalized.contains("forbidden")
                                ? HttpStatus.FORBIDDEN
                                : normalized.contains("already")
                                                || normalized.contains("conflict")
                                                || normalized.contains("modified")
                                        ? HttpStatus.CONFLICT
                                        : HttpStatus.UNPROCESSABLE_CONTENT;
        return failure(status);
    }

    /**
     * {@code invalidState} 执行当前类型定义的业务操作。
     *
     * @param error 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Result<Void>> invalidState(IllegalStateException error) {
        HttpStatus status =
                String.valueOf(error.getMessage()).toLowerCase(Locale.ROOT).contains("not found")
                        ? HttpStatus.NOT_FOUND
                        : HttpStatus.CONFLICT;
        return failure(status);
    }

    /**
     * {@code unexpected} 执行当前类型定义的业务操作。
     *
     * @param error 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> unexpected(RuntimeException error) {
        return failure(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * {@code responseStatus} 执行当前类型定义的业务操作。
     *
     * @param error 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result<Void>> responseStatus(ResponseStatusException error) {
        HttpStatus status = HttpStatus.valueOf(error.getStatusCode().value());
        return failure(status);
    }

    private ResponseEntity<Result<Void>> failure(HttpStatus status) {
        BizResultCode code =
                switch (status) {
                    case NOT_FOUND -> BizResultCode.NOT_FOUND;
                    case FORBIDDEN -> BizResultCode.FORBIDDEN;
                    case UNPROCESSABLE_CONTENT -> BizResultCode.ERR_10007;
                    case CONFLICT -> BizResultCode.ERR_10008;
                    default -> BizResultCode.ERROR;
                };
        return ResponseEntity.status(status)
                .body(Result.common(null, code.getCode(), code.getMsg(), false));
    }
}
