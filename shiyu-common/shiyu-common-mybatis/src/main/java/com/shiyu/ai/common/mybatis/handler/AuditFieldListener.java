package com.shiyu.ai.common.mybatis.handler;

import com.shiyu.ai.common.core.domain.BaseEntity;
import com.shiyu.ai.common.core.domain.LoginContextHolder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

/**
 * MyBatis-Flex 审计字段自动填充监听器
 * 在插入和更新操作时自动填充创建人、创建时间、更新人、更新时间、租户ID、空间ID
 * 租户ID优先由MyBatis-Flex多租户机制处理，此处作为TenantManager未设置时的兜底
 */
public class AuditFieldListener implements com.mybatisflex.annotation.InsertListener,
        com.mybatisflex.annotation.UpdateListener {

    @Override
    public void onInsert(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            LocalDateTime now = LocalDateTime.now();
            String currentUser = getCurrentUser();

            if (baseEntity.getCreateBy() == null || "system".equals(baseEntity.getCreateBy())) {
                baseEntity.setCreateBy(currentUser);
            }
            if (baseEntity.getCreateTime() == null) {
                baseEntity.setCreateTime(now);
            }
            if (baseEntity.getUpdateBy() == null || "system".equals(baseEntity.getUpdateBy())) {
                baseEntity.setUpdateBy(currentUser);
            }
            if (baseEntity.getUpdateTime() == null) {
                baseEntity.setUpdateTime(now);
            }
        }

        autoFillReflective(entity, "tenantId", LoginContextHolder.getTenantId());
        autoFillReflective(entity, "workspaceId", LoginContextHolder.getCurrentWorkspaceId());
    }

    @Override
    public void onUpdate(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            baseEntity.setUpdateBy(getCurrentUser());
            baseEntity.setUpdateTime(LocalDateTime.now());
        }
    }

    private void autoFillReflective(Object entity, String fieldName, Object value) {
        if (value == null) return;
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            if (field.get(entity) == null) {
                field.set(entity, value);
            }
        } catch (NoSuchFieldException ignored) {
        } catch (Exception e) {
            throw new RuntimeException("Failed to auto-fill " + fieldName, e);
        }
    }

    private String getCurrentUser() {
        try {
            if (LoginContextHolder.isLogin()) {
                String username = LoginContextHolder.getUsername();
                if (username != null && !username.isBlank()) {
                    return username;
                }
            }
        } catch (Exception ignored) {
        }
        return "system";
    }
}
