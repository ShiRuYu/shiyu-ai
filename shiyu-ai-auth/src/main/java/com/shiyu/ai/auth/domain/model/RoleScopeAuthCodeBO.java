package com.shiyu.ai.auth.domain.model;
import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 角色作用域权限授权数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleScopeAuthCodeBO extends TenantModel {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long roleId;

    private Long authCodeId;
}
