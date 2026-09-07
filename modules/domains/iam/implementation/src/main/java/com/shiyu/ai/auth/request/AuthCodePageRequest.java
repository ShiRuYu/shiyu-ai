package com.shiyu.ai.auth.request;

import com.shiyu.ai.common.core.api.PageQuery;
import lombok.EqualsAndHashCode;
import lombok.Data;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuthCodePageRequest extends PageQuery {
    private static final long serialVersionUID = 1L;
    private String code;
    private String name;
}
