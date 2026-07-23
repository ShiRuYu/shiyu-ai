package com.shiyu.ai.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 切换工作空间请求
 */
@Data
@Schema(description = "切换工作空间请求")
public class SwitchWorkspaceRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "目标工作空间ID")
    private Long workspaceId;
}
