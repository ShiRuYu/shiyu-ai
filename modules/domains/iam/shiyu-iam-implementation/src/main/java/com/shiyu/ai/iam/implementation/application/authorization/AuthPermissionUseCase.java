package com.shiyu.ai.iam.implementation.application.authorization;

import com.shiyu.ai.iam.implementation.port.repository.AuthRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 编排 AuthPermission 应用用例。
 */
@Slf4j
public final class AuthPermissionUseCase {
    /**
     * authRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthRepository authRepository;

    /**
     * {@code AuthPermissionUseCase} 创建并初始化当前类型实例。
     *
     * @param authRepository 参数值，用于执行当前操作。
     */
    public AuthPermissionUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * {@code getAuthCodes} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param username 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<String> getAuthCodes(ActorContext actor, String username) {
        log.info("获取权限编码, usernamePresent={}", username != null);
        try {
            Objects.requireNonNull(actor, "actor must not be null");
            TenantId tenantId =
                    Objects.requireNonNull(actor.tenantId(), "actor tenantId must not be null");
            List<String> codes = authRepository.selectCodesByUsername(username, tenantId);
            return codes == null || codes.isEmpty() ? new ArrayList<>() : codes;
        } catch (Exception e) {
            log.error(
                    "获取权限编码异常, usernamePresent={}, errorType={}, errorMessageLength={}",
                    username != null,
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
            return new ArrayList<>();
        }
    }

    /**
     * {@code getAuthCodesByUserId} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param userId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<String> getAuthCodesByUserId(ActorContext actor, UserId userId) {
        log.info("获取权限编码, userIdPresent={}", userId != null && userId.value() > 0);
        try {
            Objects.requireNonNull(actor, "actor must not be null");
            Objects.requireNonNull(userId, "userId must not be null");
            TenantId currentTenantId =
                    Objects.requireNonNull(actor.tenantId(), "actor tenantId must not be null");
            if (!actor.platformAdmin()
                    && !actor.parentSuperAdminSwitch()
                    && !actor.userId().equals(userId)) {
                log.warn(
                        "拒绝查询其他用户权限, actorUserIdPresent={}, targetUserIdPresent={}",
                        actor.userId().value() > 0,
                        userId.value() > 0);
                return new ArrayList<>();
            }
            String currentRoleCode = actor.activeRoleCode();
            List<String> codes;
            if (actor.parentSuperAdminSwitch() && currentRoleCode != null) {
                codes =
                        authRepository.selectCodesByRoleCodeAndTenant(
                                currentRoleCode, currentTenantId);
            } else if (currentRoleCode != null) {
                codes =
                        authRepository.selectCodesByUserIdAndRoleCode(
                                userId, currentTenantId, currentRoleCode);
            } else {
                codes = authRepository.selectCodesByUserId(userId, currentTenantId);
            }
            return codes == null || codes.isEmpty() ? new ArrayList<>() : codes;
        } catch (Exception e) {
            log.error(
                    "获取权限编码异常, userIdPresent={}, errorType={}, errorMessageLength={}",
                    userId != null && userId.value() > 0,
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
            return new ArrayList<>();
        }
    }

    private static int valueLength(String value) {
        return value == null ? 0 : value.length();
    }
}
