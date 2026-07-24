package com.shiyu.ai.auth.config;

import cn.dev33.satoken.stp.StpInterface;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.dal.auth.repository.AuthRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限码提供者。
 * 权限码按当前作用域租户查询，避免把一个用户在其他租户中的按钮权限带入当前请求。
 */
@Component
public class SaPermissionProvider implements StpInterface {

    private final AuthRepository authRepository;

    public SaPermissionProvider(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (loginId == null) {
            return Collections.emptyList();
        }
        try {
            Long userId = Long.valueOf(loginId.toString());
            return authRepository.selectCodesByUserId(userId, LoginContextHolder.getCurrentTenantId());
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return Collections.emptyList();
    }
}
