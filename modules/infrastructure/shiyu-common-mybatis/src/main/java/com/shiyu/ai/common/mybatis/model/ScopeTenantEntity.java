package com.shiyu.ai.common.mybatis.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 授权关系实体基类。
 *
 * <p>严格单租户上下文下，授权关系只归属于一个 tenantId， 不再额外维护 tenantId。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScopeTenantEntity extends TenantEntity {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;
}
