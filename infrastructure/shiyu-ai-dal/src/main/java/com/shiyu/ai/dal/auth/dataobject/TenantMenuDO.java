package com.shiyu.ai.dal.auth.dataobject;

import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("auth_tenant_menu")
public class TenantMenuDO {
    private Long tenantId;
    private Long menuId;
    private Integer status;
}
