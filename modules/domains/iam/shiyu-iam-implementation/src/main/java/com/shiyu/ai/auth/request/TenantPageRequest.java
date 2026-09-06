package com.shiyu.ai.auth.request;

import com.shiyu.ai.common.core.api.PageQuery;
import lombok.EqualsAndHashCode;
import lombok.Data;

@Data
@EqualsAndHashCode(callSuper = true)
public class TenantPageRequest extends PageQuery {
    private static final long serialVersionUID = 1L;
    private String name;
    private String code;
    private Integer status;
}
