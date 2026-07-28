package com.shiyu.ai.dal.auth.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.ScopeTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 角色作用域权限授权数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("role_scope_auth_code")
public class RoleScopeAuthCodeDO extends ScopeTenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long roleId;

    private Long authCodeId;
}
