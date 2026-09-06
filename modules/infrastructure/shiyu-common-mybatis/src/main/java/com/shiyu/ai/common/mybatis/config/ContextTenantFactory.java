package com.shiyu.ai.common.mybatis.config;

import com.mybatisflex.core.tenant.TenantFactory;
import com.shiyu.ai.kernel.context.TenantScope;
import org.springframework.stereotype.Component;

/**
 * 基于当前操作租户的严格单租户过滤工厂。
 */
@Component
@SuppressWarnings("deprecation")
public class ContextTenantFactory implements TenantFactory {

    @Override
    public Object[] getTenantIds() {
        return new Object[]{TenantScope.require().value()};
    }

    @Override
    public Object[] getTenantIds(String tableName) {
        // 所有带租户归属的数据访问都必须拥有有效上下文。
        // 需要跨租户执行的认证管理查询必须显式使用 TenantManager.withoutTenantCondition，
        // 不能通过返回 null 绕过租户过滤器。
        return getTenantIds();
    }
}
