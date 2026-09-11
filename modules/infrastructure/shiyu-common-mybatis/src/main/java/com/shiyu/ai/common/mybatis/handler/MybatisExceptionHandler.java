package com.shiyu.ai.common.mybatis.handler;

import com.shiyu.ai.common.core.api.Result;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Mybatis异常处理器 */
@Slf4j
@RestControllerAdvice
public class MybatisExceptionHandler {

    /** 主键或UNIQUE索引，数据重复异常 */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(
            DuplicateKeyException e, HttpServletRequest request) {
        log.error(
                "数据库唯一约束冲突: requestPathLength={}, errorType={}, errorMessageLength={}",
                valueLength(request.getRequestURI()),
                e.getClass().getSimpleName(),
                valueLength(e.getMessage()));
        return Result.fail("数据库中已存在该记录，请联系管理员确认");
    }

    /** Mybatis系统异常 通用处理 */
    @ExceptionHandler(MyBatisSystemException.class)
    public Result<Void> handleCannotFindDataSourceException(
            MyBatisSystemException e, HttpServletRequest request) {
        String message = e.getMessage();
        if (message != null && message.contains("CannotFindDataSourceException")) {
            log.error(
                    "未找到数据源: requestPathLength={}, errorType={}, errorMessageLength={}",
                    valueLength(request.getRequestURI()),
                    e.getClass().getSimpleName(),
                    valueLength(message));
            return Result.fail("未找到数据源，请联系管理员确认");
        }
        log.error(
                "Mybatis系统异常: requestPathLength={}, errorType={}, errorMessageLength={}",
                valueLength(request.getRequestURI()),
                e.getClass().getSimpleName(),
                valueLength(message));
        return Result.fail("数据库操作失败，请稍后重试");
    }

    private int valueLength(String value) {
        return value == null ? 0 : value.length();
    }
}
