package com.shiyu.ai.iam.implementation.application.authorization;

import com.shiyu.ai.iam.implementation.port.repository.AuthRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Permission lookup use cases scoped by the explicit actor context. */
@Slf4j
public final class AuthPermissionUseCase {
    private final AuthRepository authRepository;

    public AuthPermissionUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public List<String> getAuthCodes(ActorContext actor, String username) {
        log.info("获取权限编码, usernamePresent={}", username != null);
        try {
            Objects.requireNonNull(actor, "actor must not be null");
            TenantId tenantId = Objects.requireNonNull(actor.tenantId(), "actor tenantId must not be null");
            List<String> codes = authRepository.selectCodesByUsername(username, tenantId);
            return codes == null || codes.isEmpty() ? new ArrayList<>() : codes;
        } catch (Exception e) {
            log.error("获取权限编码异常, usernamePresent={}, errorType={}, errorMessageLength={}",
                    username != null, e.getClass().getSimpleName(), valueLength(e.getMessage()));
            return new ArrayList<>();
        }
    }

    public List<String> getAuthCodesByUserId(ActorContext actor, UserId userId) {
        log.info("获取权限编码, userIdPresent={}", userId != null && userId.value() > 0);
        try {
            Objects.requireNonNull(actor, "actor must not be null");
            Objects.requireNonNull(userId, "userId must not be null");
            TenantId currentTenantId = Objects.requireNonNull(actor.tenantId(), "actor tenantId must not be null");
            if (!actor.platformAdmin() && !actor.parentSuperAdminSwitch() && !actor.userId().equals(userId)) {
                log.warn("拒绝查询其他用户权限, actorUserIdPresent={}, targetUserIdPresent={}",
                        actor.userId().value() > 0, userId.value() > 0);
                return new ArrayList<>();
            }
            String currentRoleCode = actor.activeRoleCode();
            List<String> codes;
            if (actor.parentSuperAdminSwitch() && currentRoleCode != null) {
                codes = authRepository.selectCodesByRoleCodeAndTenant(currentRoleCode, currentTenantId);
            } else if (currentRoleCode != null) {
                codes = authRepository.selectCodesByUserIdAndRoleCode(userId, currentTenantId, currentRoleCode);
            } else {
                codes = authRepository.selectCodesByUserId(userId, currentTenantId);
            }
            return codes == null || codes.isEmpty() ? new ArrayList<>() : codes;
        } catch (Exception e) {
            log.error("获取权限编码异常, userIdPresent={}, errorType={}, errorMessageLength={}",
                    userId != null && userId.value() > 0,
                    e.getClass().getSimpleName(), valueLength(e.getMessage()));
            return new ArrayList<>();
        }
    }

    private static int valueLength(String value) {
        return value == null ? 0 : value.length();
    }
}
