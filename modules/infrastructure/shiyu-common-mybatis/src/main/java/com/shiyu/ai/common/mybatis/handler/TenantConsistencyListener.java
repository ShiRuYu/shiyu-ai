package com.shiyu.ai.common.mybatis.handler;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.kernel.context.TenantScope;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import com.mybatisflex.annotation.Column;

import java.lang.reflect.Field;

/**
 * 校验 MyBatis-Flex 租户实体的归属与当前执行作用域一致。
 *
 * <p>租户 ID 的默认填充仍由 MyBatis-Flex 完成；本监听器只负责阻止空作用域和跨租户实体
 * 进入标准 Mapper 写入路径。原生 SQL 和 JDBC 必须由调用方显式带上租户条件。
 */
public final class TenantConsistencyListener implements InsertListener, UpdateListener {

    @Override
    public void onInsert(Object entity) {
        validate(entity);
    }

    @Override
    public void onUpdate(Object entity) {
        validate(entity);
    }

    private static void validate(Object entity) {
        Long tenantId = tenantIdOf(entity);
        if (tenantId != null || isTenantEntity(entity)) {
            if (tenantId == null) {
                // Flex 会从 TenantFactory 为新增记录补齐空租户值；更新可能只提交部分实体，
                // 但两种情况都必须存在已绑定的租户作用域，监听器不会隐式创建作用域。
                TenantScope.require();
                return;
            }
            TenantScope.requireMatches(new com.shiyu.ai.kernel.context.TenantId(tenantId));
        }
    }

    private static boolean isTenantEntity(Object entity) {
        return entity instanceof TenantEntity || tenantField(entity) != null;
    }

    private static Long tenantIdOf(Object entity) {
        if (entity instanceof TenantEntity tenantEntity) {
            return tenantEntity.getTenantId();
        }
        Field field = tenantField(entity);
        if (field == null) return null;
        try {
            field.setAccessible(true);
            Object value = field.get(entity);
            return value instanceof Number number ? number.longValue() : null;
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("cannot read tenantId", exception);
        }
    }

    private static Field tenantField(Object entity) {
        Class<?> type = entity.getClass();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                Column column = field.getAnnotation(Column.class);
                if (column != null && column.tenantId()) return field;
            }
            type = type.getSuperclass();
        }
        return null;
    }
}
