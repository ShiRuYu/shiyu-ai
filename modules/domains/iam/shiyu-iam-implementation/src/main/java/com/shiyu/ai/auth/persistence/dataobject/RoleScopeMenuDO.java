package com.shiyu.ai.auth.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.ScopeTenantEntity;
import com.shiyu.ai.common.mybatis.model.ServiceAssignedTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import com.shiyu.ai.auth.domain.model.RoleScopeMenuBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("auth_role_scope_menu")
@AutoMapper(target = RoleScopeMenuBO.class, reverseConvertGenerate = true)
public class RoleScopeMenuDO extends ScopeTenantEntity implements ServiceAssignedTenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long roleId;

    private Long menuId;
}

