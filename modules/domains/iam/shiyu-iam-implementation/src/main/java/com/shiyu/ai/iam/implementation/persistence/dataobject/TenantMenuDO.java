package com.shiyu.ai.iam.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.iam.implementation.domain.model.TenantMenuBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

@Data
@Table("auth_tenant_menu")
@AutoMapper(target = TenantMenuBO.class, reverseConvertGenerate = true)
public class TenantMenuDO {
    private Long tenantId;
    private Long menuId;
    private Integer status;
}
