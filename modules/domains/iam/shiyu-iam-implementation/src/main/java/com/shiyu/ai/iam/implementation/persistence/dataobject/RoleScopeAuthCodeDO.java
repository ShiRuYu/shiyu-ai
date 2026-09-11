package com.shiyu.ai.iam.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.ScopeTenantEntity;
import com.shiyu.ai.common.mybatis.model.ServiceAssignedTenantEntity;
import com.shiyu.ai.iam.implementation.domain.model.RoleScopeAuthCodeBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/** 角色作用域权限授权数据对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("auth_role_scope_auth_code")
@AutoMapper(target = RoleScopeAuthCodeBO.class, reverseConvertGenerate = true)
public class RoleScopeAuthCodeDO extends ScopeTenantEntity implements ServiceAssignedTenantEntity {

    @Serial private static final long serialVersionUID = 1L;

    private Long roleId;

    private Long authCodeId;
}
