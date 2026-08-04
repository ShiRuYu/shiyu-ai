package com.shiyu.ai.dal.config;

import com.mybatisflex.core.tenant.TenantFactory;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.common.core.domain.UserContext;
import org.springframework.stereotype.Component;

/**
 * 基于当前操作租户的严格单租户过滤工厂。
 */
@Component
public class ContextTenantFactory implements TenantFactory {

    @Override
    public Object[] getTenantIds() {
        UserContext user = UserContextHolder.getContext();
        if (user == null) return new Object[0];

        return user.getCurrentTenantId() == null
                ? new Object[0]
                : new Object[]{user.getCurrentTenantId()};
    }

    @Override
    public Object[] getTenantIds(String tableName) {
        // 认证关系表由仓储层显式按当前上下文校验。
        if (tableName != null && java.util.Set.of(
                "auth_user",
                "auth_tenant",
                "auth_user_scope_role",
                "auth_role_scope_menu",
                "auth_role_scope_auth_code",
                "auth_tenant_menu",
                "auth_tenant_auth_code",
                "auth_tenant_quota"
        ).contains(tableName.toLowerCase())) {
            return null;
        }
        return getTenantIds();
    }
}
