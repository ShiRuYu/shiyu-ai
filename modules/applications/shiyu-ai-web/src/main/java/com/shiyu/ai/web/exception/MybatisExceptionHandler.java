package com.shiyu.ai.web.exception;

import com.shiyu.ai.common.foundation.api.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 将数据库唯一约束和 MyBatis 系统异常转换为不暴露底层连接信息的 HTTP 响应。
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class MybatisExceptionHandler {

    /**
     * 将唯一约束冲突转换为固定的业务错误信息。
     *
     * @param error 数据库抛出的唯一约束冲突
     * @param request 发生冲突的 HTTP 请求
     * @return 不包含数据库详情的失败结果
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(
            DuplicateKeyException error, HttpServletRequest request) {
        log.error(
                "数据库唯一约束冲突: requestPathLength={}, errorType={}, errorMessageLength={}",
                valueLength(request.getRequestURI()),
                error.getClass().getSimpleName(),
                valueLength(error.getMessage()));
        return Result.fail("数据库中已存在该记录，请联系管理员确认");
    }

    /**
     * 将 MyBatis 系统异常转换为固定的业务错误信息，并单独提示数据源缺失。
     *
     * @param error MyBatis 系统异常
     * @param request 触发异常的 HTTP 请求
     * @return 不包含数据源凭据和驱动详情的失败结果
     */
    @ExceptionHandler(MyBatisSystemException.class)
    public Result<Void> handleCannotFindDataSourceException(
            MyBatisSystemException error, HttpServletRequest request) {
        String message = error.getMessage();
        if (message != null && message.contains("CannotFindDataSourceException")) {
            log.error(
                    "未找到数据源: requestPathLength={}, errorType={}, errorMessageLength={}",
                    valueLength(request.getRequestURI()),
                    error.getClass().getSimpleName(),
                    valueLength(message));
            return Result.fail("未找到数据源，请联系管理员确认");
        }
        log.error(
                "Mybatis系统异常: requestPathLength={}, errorType={}, errorMessageLength={}",
                valueLength(request.getRequestURI()),
                error.getClass().getSimpleName(),
                valueLength(message));
        return Result.fail("数据库操作失败，请稍后重试");
    }

    private int valueLength(String value) {
        return value == null ? 0 : value.length();
    }
}
