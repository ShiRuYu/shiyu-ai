package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.bo.WorkspaceBO;

import java.util.List;

/**
 * 工作空间服务接口
 */
public interface WorkspaceService {

    /**
     * 获取工作空间列表（树形）
     *
     * @param name 工作空间名称（可选，用于过滤）
     * @return 工作空间树形列表
     */
    /**
     * Get Workspace List
     * @return 处理结果
     */
    List<WorkspaceBO> getWorkspaceList(String name);

    /**
     * 根据 ID 获取工作空间
     *
     * @param id 工作空间 ID
     * @return 工作空间信息
     */
    /**
     * Get By Id
     * @return 处理结果
     */
    WorkspaceBO getById(Long id);

    /**
     * 新增工作空间
     *
     * @param workspaceBO 工作空间信息
     * @return 是否成功
     */
    /**
     * Create Workspace
     * @param WorkspaceBO WorkspaceBO
     * @return 处理结果
     */
    boolean createWorkspace(WorkspaceBO workspaceBO);

    /**
     * 修改工作空间
     *
     * @param id          工作空间 ID
     * @param workspaceBO 工作空间信息
     * @return 是否成功
     */
    /**
     * Update Workspace
     * @param WorkspaceBO WorkspaceBO
     * @return 处理结果
     */
    boolean updateWorkspace(Long id, WorkspaceBO workspaceBO);

    /**
     * 删除工作空间
     *
     * @param id 工作空间 ID
     * @return 是否成功
     */
    /**
     * Delete Workspace
     * @return 处理结果
     */
    boolean deleteWorkspace(Long id);
}
