package com.shiyu.ai.agent.config;

import com.mybatisflex.core.tenant.TenantFactory;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.domain.LoginUser;
import org.springframework.stereotype.Component;

/**
 * 基于登录上下文的租户工厂
 * 由 MyBatis-Flex 在每次 SQL 执行时调用 getTenantIds() 获取当前租户标识
 */
@Component
public class ContextTenantFactory implements TenantFactory {

    @Override
    public Object[] getTenantIds() {
        LoginUser user = LoginContextHolder.getContext();
        if (user == null || user.isSuperAdmin()) {
            return null;
        }
        Long tenantId = user.getTenantId();
        return tenantId != null ? new Object[]{tenantId} : null;
    }

    @Override
    public Object[] getTenantIds(String tableName) {
        if ("tenant".equalsIgnoreCase(tableName)) {
            return null;
        }
        return getTenantIds();
    }
}
