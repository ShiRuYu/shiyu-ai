package com.shiyu.ai.dal.auth.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.ScopeTenantEntity;
import com.shiyu.ai.common.mybatis.model.ServiceAssignedTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("auth_user_scope_role")
public class UserScopeRoleDO extends ScopeTenantEntity implements ServiceAssignedTenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long roleId;
}
