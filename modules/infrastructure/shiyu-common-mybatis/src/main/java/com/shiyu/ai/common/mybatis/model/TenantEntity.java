package com.shiyu.ai.common.mybatis.model;

import com.mybatisflex.annotation.Column;
import com.shiyu.ai.common.core.domain.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/** 租户基类 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantEntity extends BaseEntity {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /** 租户ID */
    @Column(tenantId = true)
    private Long tenantId;
}
