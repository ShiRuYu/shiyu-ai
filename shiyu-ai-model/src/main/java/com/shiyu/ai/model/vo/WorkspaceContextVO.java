package com.shiyu.ai.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class WorkspaceContextVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long workspaceId;

    private String workspaceName;

    private String roleCode;
}
