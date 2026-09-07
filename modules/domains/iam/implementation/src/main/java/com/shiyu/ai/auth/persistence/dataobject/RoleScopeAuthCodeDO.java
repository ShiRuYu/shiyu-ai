package com.shiyu.ai.auth.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.ScopeTenantEntity;
import com.shiyu.ai.common.mybatis.model.ServiceAssignedTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import com.shiyu.ai.auth.domain.model.RoleScopeAuthCodeBO;
import io.github.linpeilie.annotations.AutoMapper;

/**
 * 角色作用域权限授权数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("auth_role_scope_auth_code")
@AutoMapper(target = RoleScopeAuthCodeBO.class, reverseConvertGenerate = true)
public class RoleScopeAuthCodeDO extends ScopeTenantEntity implements ServiceAssignedTenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long roleId;

    private Long authCodeId;
}

