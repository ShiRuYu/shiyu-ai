package com.shiyu.ai.dal.config;

import com.mybatisflex.core.tenant.TenantFactory;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.domain.LoginUser;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.List;

/**
 * 基于作用域租户的可见性工厂
 *
 * 可见范围规则（scope 为当前作用域租户）：
 *   1. filterTenantId 已设置 → 只查看该租户数据（精确过滤）
 *   2. superAdmin → 查看全部
 *   3. 根据 visibleTenantIds 列表查看所有可见租户数据
 */
@Component
public class ContextTenantFactory implements TenantFactory {

    @Override
    public Object[] getTenantIds() {
        LoginUser user = LoginContextHolder.getContext();
        if (user == null) return new Object[0];

        // 1. 子租户精确筛选（从可见范围内进一步限定）
        if (user.getFilterTenantId() != null) {
            return new Object[]{user.getFilterTenantId()};
        }

        // 2. 超级管理员 → 查看全部
        if (user.isSuperAdmin()) {
            return null;
        }

        // 3. 可见范围 → WHERE tenant_id IN (...)
        List<Long> visibleIds = user.getVisibleTenantIds();
        if (visibleIds == null || visibleIds.isEmpty()) {
            return new Object[0];
        }
        return visibleIds.toArray();
    }

    @Override
    public Object[] getTenantIds(String tableName) {
        if (tableName != null && Set.of(
                "user",
                "tenant",
                "user_scope_role",
                "role_scope_menu",
                "role_scope_auth_code"
        ).contains(tableName.toLowerCase())) {
            return null;
        }
        return getTenantIds();
    }
}
