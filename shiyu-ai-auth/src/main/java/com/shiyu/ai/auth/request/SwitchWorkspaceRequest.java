package com.shiyu.ai.auth.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 切换工作空间请求
 */
@Data
public class SwitchWorkspaceRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long workspaceId;
}
