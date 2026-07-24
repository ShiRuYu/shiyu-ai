package com.shiyu.ai.dal.auth.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("role_scope_menu")
public class RoleScopeMenuDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long roleId;

    private Long menuId;
}
