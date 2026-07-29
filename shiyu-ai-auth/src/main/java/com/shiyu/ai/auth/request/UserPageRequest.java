package com.shiyu.ai.auth.request;

import com.shiyu.ai.common.core.api.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户分页查询请求
 */
@Data
@Schema(description = "用户分页查询请求")
public class UserPageRequest extends PageQuery {

    @Schema(description = "用户名（模糊查询）")
    private String username;

}
