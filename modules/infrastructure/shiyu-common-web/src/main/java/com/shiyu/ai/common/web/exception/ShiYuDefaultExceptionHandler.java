package com.shiyu.ai.common.web.exception;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.core.exception.BaseBizException;
import com.shiyu.ai.common.core.exception.base.BaseException;

import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** 异常处理 */
@RestControllerAdvice
@Slf4j
public class ShiYuDefaultExceptionHandler {

    /**
     * {@code exception} 执行当前类型定义的业务操作。
     *
     * @param e 参数值，用于执行当前操作。
     * @param bindingResult 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @ExceptionHandler(BindException.class)
    public Result<String> exception(BindException e, BindingResult bindingResult) {
        log.warn(
                "请求参数绑定失败: errorType={}, errorMessageLength={}",
                e.getClass().getSimpleName(),
                messageLength(e));
        return Result.fail(BizResultCode.ERR_10006, getBindingResult(e, bindingResult));
    }

    private String getBindingResult(BindException e, BindingResult bindingResult) {
        if (bindingResult.getFieldErrors() != null && !bindingResult.getFieldErrors().isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                stringBuilder
                        .append(fieldError.getField())
                        .append(":")
                        .append(fieldError.getDefaultMessage())
                        .append("; ");
            }
            return stringBuilder.toString();
        }
        return e.getMessage();
    }

    /**
     * {@code exception} 执行当前类型定义的业务操作。
     *
     * @param e 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> exception(ConstraintViolationException e) {
        log.warn(
                "请求参数校验失败: errorType={}, errorMessageLength={}",
                e.getClass().getSimpleName(),
                messageLength(e));
        return Result.fail(BizResultCode.ERR_10007, "请求参数校验失败");
    }

    /**
     * {@code exception} 执行当前类型定义的业务操作。
     *
     * @param e 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<String> exception(HttpMessageNotReadableException e) {
        log.warn(
                "请求体格式不正确: errorType={}, errorMessageLength={}",
                e.getClass().getSimpleName(),
                messageLength(e));
        return Result.fail(BizResultCode.BAD_REQUEST, "请求体格式不正确");
   }

    /**
     * {@code missingRequestPart} 执行当前类型定义的业务操作。
     *
     * @param e 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
   @ExceptionHandler({
       MissingServletRequestParameterException.class,
       MissingServletRequestPartException.class
   })
   public Result<String> missingRequestPart(Exception e) {
       log.warn(
                "请求参数缺失: errorType={}, errorMessageLength={}",
                e.getClass().getSimpleName(),
                messageLength(e));
        return Result.fail(BizResultCode.BAD_REQUEST, "请求参数不完整");
   }

    /**
     * {@code unsupportedMediaType} 执行当前类型定义的业务操作。
     *
     * @param e 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
   @ExceptionHandler({
       HttpMediaTypeNotSupportedException.class,
       HttpMediaTypeNotAcceptableException.class
   })
   public Result<String> unsupportedMediaType(Exception e) {
       log.warn(
                "请求内容类型不正确: errorType={}, errorMessageLength={}",
                e.getClass().getSimpleName(),
                messageLength(e));
        return Result.fail(BizResultCode.BAD_REQUEST, "请求内容类型不正确");
    }

    /**
     * {@code exception} 执行当前类型定义的业务操作。
     *
     * @param e 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<String> exception(MethodArgumentTypeMismatchException e) {
        return Result.fail(BizResultCode.BAD_REQUEST, "请求参数格式不正确: " + e.getName());
    }

    /**
     * {@code exception} 执行当前类型定义的业务操作。
     *
     * @param e 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Result<String> exception(ResponseStatusException e) {
        String message =
                e.getReason() == null || e.getReason().isBlank()
                        ? e.getStatusCode().toString()
                        : e.getReason();
        return Result.common(null, e.getStatusCode().value(), message, false);
    }

    /**
     * {@code exception} 执行当前类型定义的业务操作。
     *
     * @param e 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<String> exception(NoResourceFoundException e) {
        return Result.fail(BizResultCode.NOT_FOUND, "资源不存在");
    }

    /**
     * {@code exception} 执行当前类型定义的业务操作。
     *
     * @param e 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @ExceptionHandler(SecurityException.class)
    public Result<String> exception(SecurityException e) {
        log.warn(
                "安全拦截: errorType={}, errorMessageLength={}",
                e.getClass().getSimpleName(),
                messageLength(e));
        return Result.fail(BizResultCode.FORBIDDEN, "无权限执行该操作");
    }

    /**
     * 处理invalidargument。
     *
     * @param class class 参数。
     *
     * @return 处理结果。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> invalidArgument(IllegalArgumentException e) {
        return Result.common(null, 422, e.getMessage(), false);
    }

    /**
     * 处理invalid状态。
     *
     * @param class class 参数。
     *
     * @return 处理结果。
     */
    @ExceptionHandler(IllegalStateException.class)
    public Result<String> invalidState(IllegalStateException e) {
        return Result.common(null, 409, e.getMessage(), false);
    }

    /**
     * {@code exception} 执行当前类型定义的业务操作。
     *
     * @param e 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @ExceptionHandler(BaseBizException.class)
    public Result<String> exception(BaseBizException e) {
        log.warn(
                "业务异常: errorType={}, errorMessageLength={}",
                e.getClass().getSimpleName(),
                messageLength(e));
        if (e.getCode() != null) {
            return Result.common(null, e.getCode(), e.getMessage(), false);
        }
        return Result.fail(BizResultCode.ERR_10009, e.getMessage());
    }

    /**
     * {@code exception} 执行当前类型定义的业务操作。
     *
     * @param e 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @ExceptionHandler(BaseException.class)
    public Result<String> exception(BaseException e) {
        log.error(
                "已知异常: errorType={}, errorMessageLength={}",
                e.getClass().getSimpleName(),
                messageLength(e));
        return Result.fail(e.getMessage());
    }

    /**
     * {@code exception} 执行当前类型定义的业务操作。
     *
     * @param e 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @ExceptionHandler(Exception.class)
    public Result<String> exception(Exception e) {
        log.error(
                "未处理异常: errorType={}, errorMessageLength={}",
                e.getClass().getSimpleName(),
                messageLength(e));
        return Result.fail(BizResultCode.ERROR, "服务器内部错误");
    }

    private int messageLength(Throwable exception) {
        return exception.getMessage() == null ? 0 : exception.getMessage().length();
    }
}
