package com.shiyu.ai.common.mybatis.handler;

import com.shiyu.ai.common.core.domain.BaseEntity;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

/**
 * MyBatis-Flex 审计字段自动填充监听器
 * 在插入和更新操作时自动填充创建人、创建时间、更新人、更新时间、租户ID、空间ID
 * 租户ID优先由MyBatis-Flex多租户机制处理，此处作为TenantManager未设置时的兜底
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

        autoFillIfPresent(entity, "tenantId", LoginContextHolder.getCurrentTenantId());
        autoFillIfPresent(entity, "scopedTenantId", LoginContextHolder.getCurrentTenantId());
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

    private void autoFillIfPresent(Object entity, String fieldName, Object value) {
        if (value == null) return;
        try {
            Field field = findField(entity.getClass(), fieldName);
            if (field == null) return;
            field.setAccessible(true);
            if (field.get(entity) == null) {
                field.set(entity, value);
            }
        } catch (Exception e) {
            log.warn("Failed to auto-fill {} on {}", fieldName, entity.getClass().getSimpleName(), e);
        }
    }

    private Field findField(Class<?> type, String fieldName) {
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
