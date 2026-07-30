package com.shiyu.ai.common.mybatis.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 授权关系实体基类。
 *
 * <p>严格单租户上下文下，授权关系只归属于一个 tenantId，
 * 不再额外维护 tenantId。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScopeTenantEntity extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

}
