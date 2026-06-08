package com.shiyu.ai.common.mybatis.handler;

import com.shiyu.ai.common.core.domain.BaseEntity;
import com.shiyu.ai.common.core.domain.LoginContextHolder;

import java.time.LocalDateTime;

/**
 * MyBatis-Flex 审计字段自动填充监听器
 * 在插入和更新操作时自动填充创建人、创建时间、更新人、更新时间字段
 */
public class AuditFieldListener implements com.mybatisflex.annotation.InsertListener,
        com.mybatisflex.annotation.UpdateListener {

    @Override
    public void onInsert(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            LocalDateTime now = LocalDateTime.now();
            String currentUser = getCurrentUser();

            // 如果字段为空才填充默认值，避免覆盖业务手动设置的值
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
    }

    @Override
    public void onUpdate(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            baseEntity.setUpdateBy(getCurrentUser());
            baseEntity.setUpdateTime(LocalDateTime.now());
        }
    }

    /**
     * 获取当前登录用户
     * 优先从 LoginContextHolder 获取，获取失败则返回 "system"
     */
    private String getCurrentUser() {
        try {
            if (LoginContextHolder.isLogin()) {
                String username = LoginContextHolder.getUsername();
                if (username != null && !username.isBlank()) {
                    return username;
                }
            }
        } catch (Exception ignored) {
            // 无法获取用户上下文时，使用默认值
        }
        return "system";
    }
}
