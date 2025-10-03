package com.shiyu.ai.mybatis.tenant.handle;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.google.common.collect.Lists;
import com.shiyu.ai.common.core.domain.LoginHelper;
import com.shiyu.ai.mybatis.tenant.properties.TenantProperties;
import lombok.AllArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import com.shiyu.ai.common.core.utils.StringUtils;

import java.util.List;

/**
 * 自定义租户处理器
 *
 */
@AllArgsConstructor
public class PlusTenantLineHandler implements TenantLineHandler {

    private final TenantProperties tenantProperties;

    @Override
    public Expression getTenantId() {
        String tenantId = LoginHelper.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            return new NullValue();
        }
        // 返回固定租户
        return new StringValue(tenantId);
    }

    @Override
    public boolean ignoreTable(String tableName) {
        String tenantId = LoginHelper.getTenantId();
        // 判断是否有租户
        if (StringUtils.isNotBlank(tenantId)) {
            // 不需要过滤租户的表
            List<String> excludes = tenantProperties.getExcludes();
            // 非业务表
            List<String> tables = Lists.newArrayList();
            tables.addAll(excludes);
            return tables.contains(tableName);
        }
        return true;
    }

}
