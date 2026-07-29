package com.shiyu.ai.auth.request;

import com.shiyu.ai.common.core.api.PageQuery;
import lombok.Data;

@Data
public class AuthCodePageRequest extends PageQuery {
    private String code;
    private String name;
}
