package com.shiyu.ai.common.core.domain;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import com.shiyu.ai.common.core.exception.base.BaseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理
 */
@RestControllerAdvice
@Slf4j
public class ShiYuDefaultExceptionHandler {

    @ExceptionHandler(BindException.class)
    public Result<String> exception(BindException e, BindingResult bindingResult) {
        log.error(e.getMessage(), e);
        return Result.fail(BizResultCode.ERR_10006, getBindingResult(e, bindingResult));
    }

    private String getBindingResult(BindException e, BindingResult bindingResult) {
        if (bindingResult.getFieldErrors() != null && !bindingResult.getFieldErrors().isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                stringBuilder.append(fieldError.getField())
                        .append(":")
                        .append(fieldError.getDefaultMessage())
                        .append("; ");
            }
            return stringBuilder.toString();
        }
        return e.getMessage();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> exception(ConstraintViolationException e) {
        log.error(e.getMessage(), e);
        return Result.fail(BizResultCode.ERR_10007, "请求参数校验失败");
    }

    @ExceptionHandler(SecurityException.class)
    public Result<String> exception(SecurityException e) {
        log.warn("安全拦截: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(BaseException.class)
    public Result<String> exception(BaseException e) {
        log.error("已知异常: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<String> exception(Exception e) {
        log.error(e.getMessage(), e);
        return Result.fail(BizResultCode.ERROR, "服务器内部错误");
    }
}

