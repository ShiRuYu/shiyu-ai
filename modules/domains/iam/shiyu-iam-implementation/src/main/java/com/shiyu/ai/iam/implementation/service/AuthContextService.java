package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.iam.implementation.api.response.AuthRoleResponse;
import com.shiyu.ai.iam.implementation.api.response.AuthScopeRoleResponse;
import com.shiyu.ai.iam.implementation.api.response.AuthTenantResponse;
import com.shiyu.ai.iam.implementation.api.response.AuthUserResponse;
import com.shiyu.ai.iam.implementation.port.repository.AuthUserLookupRepository;
import com.shiyu.ai.iam.implementation.port.repository.TenantRepository;
import com.shiyu.ai.kernel.context.TenantId;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AuthContextService 服务接口，负责执行身份与访问领域相关业务操作。
 */
@Service
@RequiredArgsConstructor
public class AuthContextService {
    /**
     * userLookup 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthUserLookupRepository userLookup;
    /**
     * 租户仓储，表示当前对象中的对应属性。
     */
    private final TenantRepository tenantRepository;

    /**
     * {@code user} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AuthUserResponse user(Long id) {
        return MapstructUtils.convert(userLookup.selectUserById(id), AuthUserResponse.class);
    }

    /**
     * {@code tenant} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AuthTenantResponse tenant(Long id) {
        return id == null
                ? null
                : MapstructUtils.convert(
                        userLookup.selectTenantById(new TenantId(id)), AuthTenantResponse.class);
    }

    /**
     * {@code role} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AuthRoleResponse role(Long id) {
        return MapstructUtils.convert(userLookup.selectRoleById(id), AuthRoleResponse.class);
    }

    /**
     * {@code tenantSuperRole} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AuthRoleResponse tenantSuperRole(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        return MapstructUtils.convert(
                userLookup.selectTenantSuperRole(tenantId), AuthRoleResponse.class);
    }

    /**
     * {@code scopeRoles} 执行当前类型定义的业务操作。
     *
     * @param userId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<AuthScopeRoleResponse> scopeRoles(Long userId) {
        return MapstructUtils.convert(
                userLookup.selectUserScopeRoles(userId), AuthScopeRoleResponse.class);
    }

    /**
     * {@code descendantTenantIds} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Long> descendantTenantIds(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        return tenantRepository.selectDescendantIds(tenantId);
    }
}
