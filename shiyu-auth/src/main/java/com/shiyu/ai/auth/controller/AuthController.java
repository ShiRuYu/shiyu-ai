package com.shiyu.ai.auth.controller;

import com.shiyu.ai.auth.domain.vo.LoginResponseVO;
import com.shiyu.ai.auth.domain.vo.LoginVO;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.common.core.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证管理控制器
 *
 * @author shiyu-ai
 */
@Tag(name = "认证管理", description = "用户登录、登出等认证相关接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "根据用户名和密码进行登录认证")
    @PostMapping("/login")
    public Result<LoginResponseVO> login(@Valid @RequestBody LoginVO loginVO) {
        return Result.success(authService.login(loginVO));
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "退出登录，清除认证信息")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
