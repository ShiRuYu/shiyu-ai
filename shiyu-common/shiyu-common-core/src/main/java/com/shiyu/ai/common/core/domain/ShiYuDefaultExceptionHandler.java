package com.shiyu.ai.common.core.domain;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
        if (CollectionUtils.isNotEmpty(bindingResult.getFieldErrors())) {
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
        return Result.fail(BizResultCode.ERR_10007, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<String> exception(Exception e) {
        log.error(e.getMessage(), e);
        return Result.fail(BizResultCode.ERROR, e.getMessage());
    }
}

