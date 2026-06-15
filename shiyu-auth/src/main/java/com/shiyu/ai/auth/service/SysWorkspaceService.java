package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.domain.bo.SysWorkspaceBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 工作空间服务层
 *
 * @author shiyu-ai
 */
public interface SysWorkspaceService {

    /**
     * 分页查询工作空间列表
     *
     * @param pageNumber 页码
     * @param pageSize   每页数量
     * @return 工作空间列表
     */
    Pair<Long, List<SysWorkspaceBO>> getAll(Number pageNumber, Number pageSize);

    /**
     * 根据 ID 查询工作空间
     *
     * @param workspaceId 工作空间 ID
     * @return 工作空间信息
     */
    SysWorkspaceBO getById(Long workspaceId);

    /**
     * 创建工作空间
     *
     * @param sysWorkspaceBO 工作空间信息
     * @return 创建后的工作空间信息
     */
    SysWorkspaceBO create(SysWorkspaceBO sysWorkspaceBO);

    /**
     * 更新工作空间
     *
     * @param sysWorkspaceBO 工作空间信息
     * @return 更新后的工作空间信息
     */
    SysWorkspaceBO update(SysWorkspaceBO sysWorkspaceBO);

    /**
     * 删除工作空间
     *
     * @param workspaceId 工作空间 ID
     */
    void deleteById(Long workspaceId);
}
