package com.shiyu.ai.dal.auth.enums;

import com.shiyu.ai.common.core.enums.IntEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 租户配额状态枚举（对应 DB auth_tenant_quota.status）
 */
@Getter
@AllArgsConstructor
public enum TenantQuotaStatus implements IntEnum {

    ACTIVE(0, "正常"),
    INACTIVE(1, "停用");

    private final Integer code;
    private final String desc;

    public static TenantQuotaStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}
