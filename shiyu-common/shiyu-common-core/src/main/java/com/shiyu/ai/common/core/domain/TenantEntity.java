package com.shiyu.ai.common.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;


/**
 * 租户基类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户编号
     */
    private String tenantId;

}
