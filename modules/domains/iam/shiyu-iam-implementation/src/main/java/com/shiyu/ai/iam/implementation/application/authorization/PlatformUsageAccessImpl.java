package com.shiyu.ai.iam.implementation.application.authorization;

import com.shiyu.ai.iam.contract.PlatformUsageAccess;
import com.shiyu.ai.iam.implementation.port.repository.AuthRepository;
import com.shiyu.ai.iam.implementation.port.repository.TenantRoleRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantScope;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

/**
 * 编排 平台 用量 Access Impl 所属应用流程的输入、协作和业务结果。
 */
@Component
public final class PlatformUsageAccessImpl implements PlatformUsageAccess {

    private static final long DEFAULT_TENANT_ID = 1L;
    private final TenantRoleRepository roles;
    private final UserScopeRoleRepository assignments;
    private final AuthRepository permissions;

    /**
     * 创建使用实时租户、角色分配及权限记录的平台访问校验器。
     *
     * @param roles 查询租户和角色的当前有效状态。
     * @param assignments 查询用户现有的租户角色分配。
     * @param permissions 查询当前角色的有效权限码。
     */
    public PlatformUsageAccessImpl(TenantRoleRepository roles,
            UserScopeRoleRepository assignments, AuthRepository permissions) {
        this.roles = roles;
        this.assignments = assignments;
        this.permissions = permissions;
    }

    @Override
    public boolean canReadPlatformUsage(ActorContext actor) {
        if (actor == null || actor.parentSuperAdminSwitch()) {
            return false;
        }
        if (actor.tenantId().value() != DEFAULT_TENANT_ID
                || actor.homeTenantId() == null
                || actor.homeTenantId().value() != DEFAULT_TENANT_ID
                || actor.activeRoleId() == null
                || !"super".equals(actor.activeRoleCode())
                || !TenantScope.current().filter(actor.tenantId()::equals).isPresent()) {
            return false;
        }
        var tenant = roles.selectTenantById(actor.tenantId());
        if (tenant == null || !Objects.equals(tenant.getId(), DEFAULT_TENANT_ID)
                || !Objects.equals(tenant.getStatus(), 1)
                || !Objects.equals(tenant.getDelFlag(), 0)) {
            return false;
        }
        var role = roles.selectEnabledRoleByCode(actor.tenantId(), "super");
        if (role == null || !Objects.equals(role.getId(), actor.activeRoleId().value())
                || !Objects.equals(role.getTenantId(), DEFAULT_TENANT_ID)
                || !"super".equals(role.getCode())
                || !Objects.equals(role.getStatus(), 1)
                || !Objects.equals(role.getDelFlag(), 0)) {
            return false;
        }
        boolean assigned = assignments.selectByUserIds(List.of(actor.userId().value())).stream()
                .anyMatch(assignment -> Objects.equals(assignment.getUserId(), actor.userId().value())
                        && Objects.equals(assignment.getTenantId(), DEFAULT_TENANT_ID)
                        && Objects.equals(assignment.getRoleId(), role.getId())
                        && Objects.equals(assignment.getStatus(), 1)
                        && Objects.equals(assignment.getDelFlag(), 0));
        return assigned && permissions.selectCodesByRoleId(role.getId(), actor.tenantId())
                .contains("platform:usage:read");
    }
}
