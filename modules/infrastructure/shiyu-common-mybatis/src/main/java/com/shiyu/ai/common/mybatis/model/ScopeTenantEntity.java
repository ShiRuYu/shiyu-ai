package com.shiyu.ai.common.mybatis.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 表示 Scope 租户 领域对象的业务状态和属性。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScopeTenantEntity extends TenantEntity {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;
}
