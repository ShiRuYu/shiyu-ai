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
 * 定义 认证 Permission 相关用例的输入、授权和业务结果。
 */
@Slf4j
public final class AuthPermissionUseCase {
    /**
     * authRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthRepository authRepository;

    /**
     * 执行 认证 Permission 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param authRepository 用于完成本次业务处理的 authRepository 参数。
     */
    public AuthPermissionUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * 查询 认证 Permission 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param username 用于完成本次业务处理的 username 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 认证 Permission 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param userId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
