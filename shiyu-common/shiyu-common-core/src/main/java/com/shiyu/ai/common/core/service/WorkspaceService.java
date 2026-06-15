package com.shiyu.ai.common.core.service;

/**
 * 通用 工作空间服务
 */
public interface WorkspaceService {

    /**
     * 通过工作空间ID查询工作空间名称
     *
     * @param workspaceIds 工作空间ID串逗号分隔
     * @return 工作空间名称串逗号分隔
     */
    String selectWorkspaceNameByIds(String workspaceIds);

}
