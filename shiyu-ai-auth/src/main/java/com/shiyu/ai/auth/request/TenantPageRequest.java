package com.shiyu.ai.auth.request;

import com.shiyu.ai.common.core.api.PageQuery;
import lombok.Data;

@Data
public class TenantPageRequest extends PageQuery {
    private String name;
    private String code;
    private Integer status;
}
