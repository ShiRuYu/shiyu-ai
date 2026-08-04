package com.shiyu.ai.auth.request;

import com.shiyu.ai.common.core.api.PageQuery;
import lombok.Data;

@Data
public class MenuPageRequest extends PageQuery {
    private String name;
    private String code;
    private String type;
    private Integer status;
}
