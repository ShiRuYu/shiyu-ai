package com.shiyu.ai.iam.implementation.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 表示 用户 Scope 角色 领域对象的业务状态和属性。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserScopeRoleBO extends TenantModel {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 用户标识，表示当前对象中的对应属性。
     */
    private Long userId;

    /**
     * 角色标识，表示当前对象中的对应属性。
     */
    private Long roleId;
}
