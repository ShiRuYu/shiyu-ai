package com.shiyu.ai.common.mybatis.tenant;

import com.mybatisflex.core.tenant.TenantFactory;
import com.shiyu.ai.kernel.context.TenantScope;

import org.springframework.stereotype.Component;

/**
 * 创建或提供 Context 租户 相关的业务组件和运行时能力。
 */
@Component
@SuppressWarnings("deprecation")
public class ContextTenantFactory implements TenantFactory {

    /**
     * 查询 Context 租户 相关业务数据，并返回处理结果。
     *
     * @return 返回 Context 租户 相关操作生成的结果数据。
     */
    @Override
    public Object[] getTenantIds() {
        return new Object[] {TenantScope.require().value()};
    }

    /**
     * 查询 Context 租户 相关业务数据，并返回处理结果。
     *
     * @param tableName 用于完成本次业务处理的 tableName 参数。
     * @return 返回 Context 租户 相关操作生成的结果数据。
     */
    @Override
    public Object[] getTenantIds(String tableName) {
        // 所有带租户归属的数据访问都必须拥有有效上下文。
        // 需要跨租户执行的认证管理查询必须显式使用 TenantQueryExecutor，
        // 不能通过返回 null 绕过租户过滤器。
        return getTenantIds();
    }
}
