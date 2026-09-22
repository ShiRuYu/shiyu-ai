package com.shiyu.ai.iam.implementation.domain.model;

import com.shiyu.ai.common.foundation.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 表示 角色 Scope 认证 Code 领域对象的业务状态和属性。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleScopeAuthCodeBO extends TenantModel {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 角色标识，表示当前对象中的对应属性。
     */
    private Long roleId;

    /**
     * authCodeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long authCodeId;
}
