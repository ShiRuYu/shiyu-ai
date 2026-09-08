package com.shiyu.ai.common.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * Domain-side tenant scope model. Persistence-specific tenant entities remain
 * in shiyu-common-mybatis; business modules use this model instead.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantModel extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long tenantId;
}
