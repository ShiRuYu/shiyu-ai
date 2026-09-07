package com.shiyu.ai.web.auth;

import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import cn.dev33.satoken.stp.StpInterface;
import com.shiyu.ai.auth.port.repository.AuthRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.UserId;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * HTTP-edge permission adapter. The authentication thread context is translated
 * once into ActorContext; IAM services and repositories remain thread-context free.
 */
@Component
public class SaPermissionProvider implements StpInterface {

    private final AuthRepository authRepository;

    public SaPermissionProvider(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (loginId == null) return Collections.emptyList();
        try {
            UserId userId = new UserId(Long.parseLong(loginId.toString()));
            ActorContext actor = ActorContextHttpAdapter.currentActor();
            if (!actor.userId().equals(userId)) return Collections.emptyList();
            String roleCode = actor.activeRoleCode();
            if (roleCode == null || roleCode.isBlank()) return Collections.emptyList();
            if (actor.parentSuperAdminSwitch()) {
                return authRepository.selectCodesByRoleCodeAndTenant(
                        roleCode, actor.tenantId());
            }
            return authRepository.selectCodesByUserIdAndRoleCode(
                    userId, actor.tenantId(), roleCode);
        } catch (RuntimeException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (loginId == null) return Collections.emptyList();
        try {
            ActorContext actor = ActorContextHttpAdapter.currentActor();
            UserId userId = new UserId(Long.parseLong(loginId.toString()));
            if (!actor.userId().equals(userId)) return Collections.emptyList();
            return authRepository.selectRoleCodesByUserId(userId, actor.tenantId());
        } catch (RuntimeException ignored) {
            return Collections.emptyList();
        }
    }
}
