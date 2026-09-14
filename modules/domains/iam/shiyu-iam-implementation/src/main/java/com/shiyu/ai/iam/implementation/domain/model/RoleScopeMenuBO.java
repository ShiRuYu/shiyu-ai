package com.shiyu.ai.iam.implementation.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * {@code RoleScopeMenuBO} 是模型模块的业务对象，承载用例处理所需的领域数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleScopeMenuBO extends TenantModel {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 角色标识，表示当前对象中的对应属性。
     */
    private Long roleId;

    /**
     * menuId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long menuId;
}
