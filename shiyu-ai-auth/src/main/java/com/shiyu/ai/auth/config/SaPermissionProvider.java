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
            Long tenantId = LoginContextHolder.getCurrentTenantId();
            String roleCode = LoginContextHolder.getCurrentRoleCode();
            if (LoginContextHolder.isParentSuperAdminSwitch()
                    && roleCode != null && tenantId != null) {
                return authRepository.selectCodesByRoleCodeAndTenant(roleCode, tenantId);
            }
            // 按用户 + 租户 + 当前角色编码查询权限码，精准控制越权
            if (roleCode != null && tenantId != null) {
                return authRepository.selectCodesByUserIdAndRoleCode(userId, tenantId, roleCode);
            }
            // 兜底：没有角色或租户时，返回空列表
            return Collections.emptyList();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (loginId == null) {
            return Collections.emptyList();
        }
        try {
            Long userId = Long.valueOf(loginId.toString());
            Long tenantId = LoginContextHolder.getCurrentTenantId();
            if (tenantId == null) {
                return Collections.emptyList();
            }
            return authRepository.selectRoleCodesByUserId(userId, tenantId);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
