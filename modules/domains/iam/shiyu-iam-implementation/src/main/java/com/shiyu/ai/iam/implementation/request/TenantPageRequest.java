package com.shiyu.ai.iam.implementation.request;

import com.shiyu.ai.common.core.api.PageQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TenantPageRequest extends PageQuery {
    private static final long serialVersionUID = 1L;
    private String name;
    private String code;
    private Integer status;
}
