package com.shiyu.ai.web.auth;

import cn.dev33.satoken.stp.StpInterface;

import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.iam.implementation.port.repository.AuthRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.UserId;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * SaPermissionProvider 边界接口，负责向外部组件提供应用领域相关能力。
 */
@Component
public class SaPermissionProvider implements StpInterface {

    /**
     * authRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthRepository authRepository;

    /**
     * {@code SaPermissionProvider} 创建并初始化当前类型实例。
     *
     * @param authRepository 参数值，用于执行当前操作。
     */
    public SaPermissionProvider(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * {@code getPermissionList} 查询并返回当前操作所需的数据。
     *
     * @param loginId 参数值，用于执行当前操作。
     * @param loginType 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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
                return authRepository.selectCodesByRoleCodeAndTenant(roleCode, actor.tenantId());
            }
            return authRepository.selectCodesByUserIdAndRoleCode(
                    userId, actor.tenantId(), roleCode);
        } catch (RuntimeException ignored) {
            return Collections.emptyList();
        }
    }

    /**
     * {@code getRoleList} 查询并返回当前操作所需的数据。
     *
     * @param loginId 参数值，用于执行当前操作。
     * @param loginType 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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
