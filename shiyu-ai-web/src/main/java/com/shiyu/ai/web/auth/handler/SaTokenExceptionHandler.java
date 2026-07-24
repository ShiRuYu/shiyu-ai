package com.shiyu.ai.web.auth.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token 全局异常处理器
 * 统一处理认证和授权相关的异常
 */
@Slf4j
@RestControllerAdvice
public class SaTokenExceptionHandler {

    /**
     * 未登录或 Token 无效
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        String message = switch (e.getType()) {
            case NotLoginException.NOT_TOKEN -> "未提供认证令牌";
            case NotLoginException.INVALID_TOKEN -> "认证令牌无效或已过期";
            case NotLoginException.TOKEN_TIMEOUT -> "认证令牌已过期，请重新登录";
            case NotLoginException.BE_REPLACED -> "您的账号已在其他设备登录，请重新登录";
            case NotLoginException.KICK_OUT -> "您的账号已被强制下线";
            default -> "未登录或登录已失效";
        };
        
        log.warn("认证失败: type={}, message={}", e.getType(), message);
        return Result.fail(BizResultCode.UNAUTHORIZED, message);
    }

    /**
     * 权限不足
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("权限不足: permission={}", e.getPermission());
        return Result.fail(BizResultCode.FORBIDDEN, BizResultCode.FORBIDDEN.getMsg());
    }

    /**
     * 角色不足
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        log.warn("角色不足: role={}", e.getRole());
        return Result.fail(BizResultCode.FORBIDDEN, "角色权限不足，请联系管理员");
    }
}
