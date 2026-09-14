package com.shiyu.ai.iam.implementation.request;

import com.shiyu.ai.common.core.api.PageQuery;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 字典分页查询请求 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典分页查询请求")
public class DictPageRequest extends PageQuery {
    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final long serialVersionUID = 1L;
}
