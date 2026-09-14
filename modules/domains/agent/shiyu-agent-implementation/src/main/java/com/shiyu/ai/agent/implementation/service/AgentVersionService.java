package com.shiyu.ai.agent.implementation.service;

import com.shiyu.ai.agent.implementation.request.EdgeRequest;
import com.shiyu.ai.agent.implementation.request.GraphConfigRequest;
import com.shiyu.ai.agent.implementation.request.NodeConfigRequest;
import com.shiyu.ai.agent.implementation.request.VersionRequest;
import com.shiyu.ai.agent.implementation.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.implementation.vo.AgentVersionVO;
import com.shiyu.ai.agent.implementation.vo.GraphValidationVO;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/** Agent Version 接口 合并了版本管理和 Graph 配置管理（节点、边、画布）。 */
public interface AgentVersionService {

    // ========== 版本基础 CRUD ==========

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<AgentVersionVO> getVersions(ActorContext actor, String agentId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     *
     * @return 操作结果。
     */
    AgentVersionDetailVO getVersionDetail(ActorContext actor, String agentId, Long versionId);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    AgentVersionVO createVersion(ActorContext actor, String agentId, VersionRequest request);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    AgentVersionVO updateVersion(
            ActorContext actor, String agentId, Long versionId, VersionRequest request);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     */
    void deleteVersion(ActorContext actor, String agentId, Long versionId);

    // ========== 版本生命周期 ==========

    /**
     * 发布或发送业务事件。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     */
    void publishVersion(ActorContext actor, String agentId, Long versionId);

    /**
     * 执行 {@code archiveVersion} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     */
    void archiveVersion(ActorContext actor, String agentId, Long versionId);

    /**
     * 执行 {@code activateVersion} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     */
    void activateVersion(ActorContext actor, String agentId, Long versionId);

    /**
     * 执行 {@code copyVersion} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    AgentVersionVO copyVersion(ActorContext actor, String agentId, VersionRequest request);

    // ========== Graph 配置 ==========

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     *
     * @return 操作结果。
     */
    AgentVersionDetailVO getGraphConfig(ActorContext actor, String agentId, Long versionId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    AgentVersionDetailVO updateGraphConfig(
            ActorContext actor, String agentId, Long versionId, GraphConfigRequest request);

    /**
     * 校验输入参数或当前业务状态。
     *
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    GraphValidationVO validateGraphConfig(GraphConfigRequest request);

    // ========== 节点管理 ==========

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     * @param request 请求参数。
     */
    void addNode(ActorContext actor, String agentId, Long versionId, NodeConfigRequest request);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     * @param nodeId 方法参数。
     * @param request 请求参数。
     */
    void updateNode(
            ActorContext actor,
            String agentId,
            Long versionId,
            String nodeId,
            NodeConfigRequest request);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     * @param nodeId 方法参数。
     */
    void deleteNode(ActorContext actor, String agentId, Long versionId, String nodeId);

    // ========== 边管理 ==========

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     * @param request 请求参数。
     */
    void addEdge(ActorContext actor, String agentId, Long versionId, EdgeRequest request);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     * @param sourceNodeId 方法参数。
     * @param targetNodeId 方法参数。
     */
    void deleteEdge(
            ActorContext actor,
            String agentId,
            Long versionId,
            String sourceNodeId,
            String targetNodeId);

    // ========== 画布管理 ==========

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     *
     * @return 操作结果。
     */
    String getCanvasConfig(ActorContext actor, String agentId, Long versionId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param agentId 方法参数。
     * @param versionId 方法参数。
     * @param canvasConfig 方法参数。
     */
    void updateCanvasConfig(
            ActorContext actor, String agentId, Long versionId, String canvasConfig);
}
