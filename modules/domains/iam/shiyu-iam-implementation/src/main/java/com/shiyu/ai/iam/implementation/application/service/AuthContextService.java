package com.shiyu.ai.iam.implementation.application.service;

import com.shiyu.ai.common.foundation.utils.MapstructUtils;
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
 * 提供 认证 Context 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 执行 认证 Context 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 认证 Context 相关操作生成的结果数据。
     */
    public AuthUserResponse user(Long id) {
        return MapstructUtils.convert(userLookup.selectUserById(id), AuthUserResponse.class);
    }

    /**
     * 执行 认证 Context 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 认证 Context 相关操作生成的结果数据。
     */
    public AuthTenantResponse tenant(Long id) {
        return id == null
                ? null
                : MapstructUtils.convert(
                        userLookup.selectTenantById(new TenantId(id)), AuthTenantResponse.class);
    }

    /**
     * 执行 认证 Context 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 认证 Context 相关操作生成的结果数据。
     */
    public AuthRoleResponse role(Long id) {
        return MapstructUtils.convert(userLookup.selectRoleById(id), AuthRoleResponse.class);
    }

    /**
     * 执行 认证 Context 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 认证 Context 相关操作生成的结果数据。
     */
    public AuthRoleResponse tenantSuperRole(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        return MapstructUtils.convert(
                userLookup.selectTenantSuperRole(tenantId), AuthRoleResponse.class);
    }

    /**
     * 执行 认证 Context 相关业务数据，并返回处理结果。
     *
     * @param userId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<AuthScopeRoleResponse> scopeRoles(Long userId) {
        return MapstructUtils.convert(
                userLookup.selectUserScopeRoles(userId), AuthScopeRoleResponse.class);
    }

    /**
     * 执行 认证 Context 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Long> descendantTenantIds(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        return tenantRepository.selectDescendantIds(tenantId);
    }
}
