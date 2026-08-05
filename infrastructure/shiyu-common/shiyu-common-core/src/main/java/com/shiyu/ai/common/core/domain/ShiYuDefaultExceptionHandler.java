package com.shiyu.ai.common.core.domain;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.core.exception.BaseBizException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import com.shiyu.ai.common.core.exception.base.BaseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 异常处理
 */
@RestControllerAdvice
@Slf4j
public class ShiYuDefaultExceptionHandler {

    @ExceptionHandler(BindException.class)
    public Result<String> exception(BindException e, BindingResult bindingResult) {
        log.warn("请求参数绑定失败: {}", e.getMessage());
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
        log.warn("请求参数校验失败: {}", e.getMessage());
        return Result.fail(BizResultCode.ERR_10007, "请求参数校验失败");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<String> exception(HttpMessageNotReadableException e) {
        log.warn("请求体格式不正确: {}", e.getMessage());
        return Result.fail(BizResultCode.BAD_REQUEST, "请求体格式不正确");
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    public Result<String> missingRequestPart(Exception e) {
        log.warn("请求参数缺失: {}", e.getMessage());
        return Result.fail(BizResultCode.BAD_REQUEST, "请求参数不完整");
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class, HttpMediaTypeNotAcceptableException.class})
    public Result<String> unsupportedMediaType(Exception e) {
        log.warn("请求内容类型不正确: {}", e.getMessage());
        return Result.fail(BizResultCode.BAD_REQUEST, "请求内容类型不正确");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<String> exception(MethodArgumentTypeMismatchException e) {
        return Result.fail(BizResultCode.BAD_REQUEST, "请求参数格式不正确: " + e.getName());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Result<String> exception(ResponseStatusException e) {
        String message = e.getReason() == null || e.getReason().isBlank()
                ? e.getStatusCode().toString() : e.getReason();
        return Result.common(null, e.getStatusCode().value(), message, false);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<String> exception(NoResourceFoundException e) {
        return Result.fail(BizResultCode.NOT_FOUND, "资源不存在");
    }

    @ExceptionHandler(SecurityException.class)
    public Result<String> exception(SecurityException e) {
        log.warn("安全拦截: {}", e.getMessage());
        return Result.fail(BizResultCode.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(BaseBizException.class)
    public Result<String> exception(BaseBizException e) {
        log.warn("业务异常: {}", e.getMessage());
        if (e.getCode() != null) {
            return Result.common(null, e.getCode(), e.getMessage(), false);
        }
        return Result.fail(BizResultCode.ERR_10009, e.getMessage());
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

