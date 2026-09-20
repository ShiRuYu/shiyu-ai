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
 * 创建或提供 Sa Permission 相关的业务组件和运行时能力。
 */
@Component
public class SaPermissionProvider implements StpInterface {

    /**
     * authRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthRepository authRepository;

    /**
     * 执行 Sa Permission 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param authRepository 用于完成本次业务处理的 authRepository 参数。
     */
    public SaPermissionProvider(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * 查询 Sa Permission 相关业务数据，并返回处理结果。
     *
     * @param loginId 用于定位login的标识。
     * @param loginType 用于完成本次业务处理的 loginType 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 Sa Permission 相关业务数据，并返回处理结果。
     *
     * @param loginId 用于定位login的标识。
     * @param loginType 用于完成本次业务处理的 loginType 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
