package com.shiyu.ai.common.mybatis.handler;

import com.shiyu.ai.common.core.domain.BaseEntity;
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

    private String getCurrentUser() {
        // 审计字段不能从线程上下文推断身份。命令/Repository 应在持久化前显式设置
        // createBy/updateBy；未提供时保留稳定占位值，避免把认证上下文泄漏到 MyBatis 层。
        return "unknown";
    }
}
