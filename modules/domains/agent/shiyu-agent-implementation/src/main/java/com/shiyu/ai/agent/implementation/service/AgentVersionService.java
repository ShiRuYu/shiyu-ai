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

/**
 * 提供 智能体 Version 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface AgentVersionService {

    // ========== 版本基础 CRUD ==========

    /**
     * 查询 智能体 Version 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<AgentVersionVO> getVersions(ActorContext actor, String agentId);

    /**
     * 查询 智能体 Version 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     * @return 返回 智能体 Version 相关操作生成的结果数据。
     */
    AgentVersionDetailVO getVersionDetail(ActorContext actor, String agentId, Long versionId);

    /**
     * 创建或保存 智能体 Version 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 智能体 Version 相关操作生成的结果数据。
     */
    AgentVersionVO createVersion(ActorContext actor, String agentId, VersionRequest request);

    /**
     * 更新或设置 智能体 Version 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 智能体 Version 相关操作生成的结果数据。
     */
    AgentVersionVO updateVersion(
            ActorContext actor, String agentId, Long versionId, VersionRequest request);

    /**
     * 删除或移除 智能体 Version 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     */
    void deleteVersion(ActorContext actor, String agentId, Long versionId);

    // ========== 版本生命周期 ==========

    /**
     * 发布或发送 智能体 Version 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     */
    void publishVersion(ActorContext actor, String agentId, Long versionId);

    /**
     * 执行 智能体 Version 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     */
    void archiveVersion(ActorContext actor, String agentId, Long versionId);

    /**
     * 执行 智能体 Version 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     */
    void activateVersion(ActorContext actor, String agentId, Long versionId);

    /**
     * 执行 智能体 Version 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 智能体 Version 相关操作生成的结果数据。
     */
    AgentVersionVO copyVersion(ActorContext actor, String agentId, VersionRequest request);

    // ========== Graph 配置 ==========

    /**
     * 查询 智能体 Version 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     * @return 返回 智能体 Version 相关操作生成的结果数据。
     */
    AgentVersionDetailVO getGraphConfig(ActorContext actor, String agentId, Long versionId);

    /**
     * 更新或设置 智能体 Version 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 智能体 Version 相关操作生成的结果数据。
     */
    AgentVersionDetailVO updateGraphConfig(
            ActorContext actor, String agentId, Long versionId, GraphConfigRequest request);

    /**
     * 校验或判断 智能体 Version 相关业务数据，并返回处理结果。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 智能体 Version 相关操作生成的结果数据。
     */
    GraphValidationVO validateGraphConfig(GraphConfigRequest request);

    // ========== 节点管理 ==========

    /**
     * 创建或保存 智能体 Version 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     * @param request 封装本次操作所需业务字段的请求对象。
     */
    void addNode(ActorContext actor, String agentId, Long versionId, NodeConfigRequest request);

    /**
     * 更新或设置 智能体 Version 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     * @param nodeId 用于定位node的标识。
     * @param request 封装本次操作所需业务字段的请求对象。
     */
    void updateNode(
            ActorContext actor,
            String agentId,
            Long versionId,
            String nodeId,
            NodeConfigRequest request);

    /**
     * 删除或移除 智能体 Version 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     * @param nodeId 用于定位node的标识。
     */
    void deleteNode(ActorContext actor, String agentId, Long versionId, String nodeId);

    // ========== 边管理 ==========

    /**
     * 创建或保存 智能体 Version 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     * @param request 封装本次操作所需业务字段的请求对象。
     */
    void addEdge(ActorContext actor, String agentId, Long versionId, EdgeRequest request);

    /**
     * 删除或移除 智能体 Version 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     * @param sourceNodeId 用于定位source的标识。
     * @param targetNodeId 用于定位target的标识。
     */
    void deleteEdge(
            ActorContext actor,
            String agentId,
            Long versionId,
            String sourceNodeId,
            String targetNodeId);

    // ========== 画布管理 ==========

    /**
     * 查询 智能体 Version 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     * @return 返回 智能体 Version 相关操作生成的结果数据。
     */
    String getCanvasConfig(ActorContext actor, String agentId, Long versionId);

    /**
     * 更新或设置 智能体 Version 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param versionId 用于定位version的标识。
     * @param canvasConfig 用于完成本次业务处理的 canvasConfig 参数。
     */
    void updateCanvasConfig(
            ActorContext actor, String agentId, Long versionId, String canvasConfig);
}
