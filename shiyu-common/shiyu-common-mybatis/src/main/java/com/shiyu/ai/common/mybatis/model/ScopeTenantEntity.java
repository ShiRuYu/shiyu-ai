package com.shiyu.ai.common.mybatis.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 带业务作用域租户的实体基类。
 *
 * <p>仅用于用户-角色、角色-菜单、角色-权限码等授权关系数据。
 * {@code tenantId} 表示数据归属租户，{@code scopedTenantId} 表示授权生效的租户作用域。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScopeTenantEntity extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 授权生效的租户作用域 ID。
     */
    private Long scopedTenantId;
}
