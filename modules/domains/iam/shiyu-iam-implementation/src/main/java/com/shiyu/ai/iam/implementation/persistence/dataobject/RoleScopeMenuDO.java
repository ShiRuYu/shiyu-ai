package com.shiyu.ai.iam.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.ScopeTenantEntity;
import com.shiyu.ai.common.mybatis.model.ServiceAssignedTenantEntity;
import com.shiyu.ai.iam.implementation.domain.model.RoleScopeMenuBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * {@code RoleScopeMenuDO} 是平台模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("auth_role_scope_menu")
@AutoMapper(target = RoleScopeMenuBO.class, reverseConvertGenerate = true)
public class RoleScopeMenuDO extends ScopeTenantEntity implements ServiceAssignedTenantEntity {

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
