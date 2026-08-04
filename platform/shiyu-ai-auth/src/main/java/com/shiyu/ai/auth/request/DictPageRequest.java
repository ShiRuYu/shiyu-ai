package com.shiyu.ai.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import com.shiyu.ai.common.core.api.PageQuery;

/**
 * 字典分页查询请求
 */
@Data
@Schema(description = "字典分页查询请求")
public class DictPageRequest extends PageQuery {
}
