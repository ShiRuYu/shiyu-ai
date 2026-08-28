package com.shiyu.ai.auth.domain.model;
import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleScopeMenuBO extends TenantModel {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long roleId;

    private Long menuId;
}
