package com.shiyu.ai.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量分配用户角色请求
 */
@Data
@Schema(description = "分配/移除用户角色请求")
public class AssignUserRolesRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID 列表
     */
    @Schema(description = "需分配/移除角色的用户ID列表")
    private List<Long> userIds;
}
