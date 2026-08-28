package com.shiyu.ai.auth.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import com.shiyu.ai.auth.domain.model.TenantMenuBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@Table("auth_tenant_menu")
@AutoMapper(target = TenantMenuBO.class, reverseConvertGenerate = true)
public class TenantMenuDO {
    private Long tenantId;
    private Long menuId;
    private Integer status;
}

