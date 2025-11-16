package com.shiyu.ai.common.mybatis.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 租户基类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantEntity extends BaseEntity {

    /**
     * 租户编号
     */
    private String tenantId;

}
