package com.shiyu.ai.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户分页查询请求
 */
@Data
@Schema(description = "用户分页查询请求")
public class UserPageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名（模糊查询）")
    private String username;

    @Schema(description = "页码（从1开始）")
    private Integer pageNo;

    @Schema(description = "每页条数")
    private Integer pageSize;
}
