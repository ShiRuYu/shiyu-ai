package com.shiyu.ai.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.shiyu.ai.common.core.api.PageQuery;

/**
 * 字典分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典分页查询请求")
public class DictPageRequest extends PageQuery {
    private static final long serialVersionUID = 1L;
}
