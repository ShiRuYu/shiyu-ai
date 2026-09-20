package com.shiyu.ai.iam.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.ScopeTenantEntity;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 表示 用户 Scope 角色 对应的持久化数据对象及其数据库字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("auth_user_scope_role")
@AutoMapper(target = UserScopeRoleBO.class, reverseConvertGenerate = true)
public class UserScopeRoleDO extends ScopeTenantEntity {

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
