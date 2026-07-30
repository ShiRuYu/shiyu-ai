package com.shiyu.ai.common.mybatis.handler;

import com.shiyu.ai.common.core.domain.BaseEntity;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.mybatis.model.ServiceAssignedTenantEntity;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * MyBatis-Flex 审计字段自动填充监听器
 * 在插入和更新操作时自动填充创建人、创建时间、更新人、更新时间。
 *
 * <p>tenantId 属于业务归属和授权作用域字段，必须由业务 Service
 * 根据目标租户显式赋值，不能根据当前登录上下文猜测。</p>
 */
@Slf4j
public class AuditFieldListener implements com.mybatisflex.annotation.InsertListener,
        com.mybatisflex.annotation.UpdateListener {

    @Override
    public void onInsert(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            LocalDateTime now = LocalDateTime.now();
            String currentUser = getCurrentUser();

            if (baseEntity.getCreateBy() == null) {
                baseEntity.setCreateBy(currentUser);
            }
            if (baseEntity.getCreateTime() == null) {
                baseEntity.setCreateTime(now);
            }
            if (baseEntity.getUpdateBy() == null) {
                baseEntity.setUpdateBy(currentUser);
            }
            if (baseEntity.getUpdateTime() == null) {
                baseEntity.setUpdateTime(now);
            }
        }

        // auth 授权域实体的 tenantId 必须由 Service 显式设置。
        // 其他尚未迁移的业务实体暂保留兼容性兜底，避免本次 auth 改造影响业务模块。
        if (!(entity instanceof ServiceAssignedTenantEntity)) {
            fillLegacyTenantFields(entity);
        }
    }

    @Override
    public void onUpdate(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            if (baseEntity.getUpdateBy() == null) {
                baseEntity.setUpdateBy(getCurrentUser());
            }
            if (baseEntity.getUpdateTime() == null) {
                baseEntity.setUpdateTime(LocalDateTime.now());
            }
        }
    }

    private void fillLegacyTenantFields(Object entity) {
        fillIfPresent(entity, "tenantId", LoginContextHolder.getCurrentTenantId());
    }

    private void fillIfPresent(Object entity, String fieldName, Object value) {
        if (value == null) {
            return;
        }
        try {
            var field = findField(entity.getClass(), fieldName);
            if (field == null) {
                return;
            }
            field.setAccessible(true);
            if (field.get(entity) == null) {
                field.set(entity, value);
            }
        } catch (Exception e) {
            log.warn("Failed to auto-fill {} on {}", fieldName, entity.getClass().getSimpleName(), e);
        }
    }

    private java.lang.reflect.Field findField(Class<?> type, String fieldName) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private String getCurrentUser() {
        if (LoginContextHolder.isLogin()) {
            String username = LoginContextHolder.getUsername();
            if (username != null && !username.isBlank()) {
                return username;
            }
        }
        log.warn("Current user is not logged");
        return "unknown";
    }
}
