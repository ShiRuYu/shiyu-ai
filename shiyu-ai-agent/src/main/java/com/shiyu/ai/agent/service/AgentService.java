package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.AgentDefinition;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * Agent Service 接口
 * 提供 Agent 定义管理、版本控制和执行能力
 */
public interface AgentService {
    
    /**
     * 注册 Agent 定义
     * @param agentDefinition Agent 定义对象
     */
    /**
     * Register Agent
     * @param AgentDefinition AgentDefinition
     * @return 处理结果
     */
    void registerAgent(AgentDefinition agentDefinition);
    
    /**
     * 获取 Agent 定义
     * @param agentId Agent ID
     * @return AgentDefinition 实例，不存在则返回 null
     */
    /**
     * Get Agent
     * @return 处理结果
     */
    AgentDefinition getAgent(String agentId);
    
    /**
     * 注销 Agent 定义
     * @param agentId Agent ID
     * @return true-注销成功，false-Agent 不存在
     */
    /**
     * Unregister Agent
     * @return 处理结果
     */
    boolean unregisterAgent(String agentId);
    
    /**
     * 执行 Agent（同步）
     * 从 Agent 定义中获取版本，从版本中获取 graph 进行编译执行
     * @param agentId Agent ID
     * @param input 输入数据
     * @return 执行结果
     * @throws Exception 执行异常
     */
    Map<String, Object> execute(String agentId, Map<String, Object> input) throws Exception;
    
    /**
     * 执行 Agent（指定版本）
     * 从 Agent 定义中获取指定版本，从版本中获取 graph 进行编译执行
     * @param agentId Agent ID
     * @param version 版本号
     * @param input 输入数据
     * @return 执行结果
     * @throws Exception 执行异常
     */
    Map<String, Object> execute(String agentId, String version, Map<String, Object> input) throws Exception;
    
    /**
     * 执行 Agent（流式）
     * @param agentId Agent ID
     * @param input 输入数据
     * @return 流式输出
     * @throws Exception 执行异常
     */
    Flux<Map<String, Object>> executeStream(String agentId, Map<String, Object> input) throws Exception;

    /**
     * 执行 Agent（指定版本，流式）
     * @param agentId Agent ID
     * @param version 版本号
     * @param input 输入数据
     * @return 流式输出
     * @throws Exception 执行异常
     */
    Flux<Map<String, Object>> executeStream(String agentId, String version, Map<String, Object> input) throws Exception;

    /**
     * 切换 Agent 当前版本
     * @param agentId Agent ID
     * @param version 版本号
     * @return true-切换成功，false-切换失败
     */
    /**
     * Switch Version
     * @return 处理结果
     */
    boolean switchVersion(String agentId, String version);

    /**
     * 列出所有已注册的 Agent
     * @return AgentDefinition 列表
     */
    /**
     * List Agents
     * @return 处理结果
     */
    List<AgentDefinition> listAgents();

    /**
     * 清理 Agent 的运行时缓存（本地内存 + AgentCacheManager）
     * 当 Agent 配置被修改后调用，确保下次执行时重新加载
     * @param agentId Agent ID
     */
    /**
     * Evict Runtime Cache
     * @return 处理结果
     */
    void evictRuntimeCache(String agentId);
}
