package com.shiyu.ai.dal.auth.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.ScopeTenantEntity;
import com.shiyu.ai.common.mybatis.model.ServiceAssignedTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import com.shiyu.ai.auth.domain.model.UserScopeRoleBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("auth_user_scope_role")
@AutoMapper(target = UserScopeRoleBO.class, reverseConvertGenerate = true)
public class UserScopeRoleDO extends ScopeTenantEntity implements ServiceAssignedTenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long roleId;
}
