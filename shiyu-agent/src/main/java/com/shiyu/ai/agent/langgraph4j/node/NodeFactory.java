package com.shiyu.ai.agent.langgraph4j.node;

import com.shiyu.ai.agent.langgraph4j.node.agent.AgentCallConfig;
import com.shiyu.ai.agent.langgraph4j.node.agent.AgentCallNode;
import com.shiyu.ai.agent.langgraph4j.node.condition.ConditionConfig;
import com.shiyu.ai.agent.langgraph4j.node.condition.ConditionNode;
import com.shiyu.ai.agent.langgraph4j.node.intent.IntentConfig;
import com.shiyu.ai.agent.langgraph4j.node.intent.IntentNode;
import com.shiyu.ai.agent.langgraph4j.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.langgraph4j.node.llm.LlmCallNode;
import com.shiyu.ai.agent.langgraph4j.node.memory.LongTermMemoryConfig;
import com.shiyu.ai.agent.langgraph4j.node.memory.LongTermMemoryNode;
import com.shiyu.ai.agent.langgraph4j.node.memory.MemoryRetrievalConfig;
import com.shiyu.ai.agent.langgraph4j.node.memory.MemoryRetrievalNode;
import com.shiyu.ai.agent.langgraph4j.node.memory.ShortTermMemoryConfig;
import com.shiyu.ai.agent.langgraph4j.node.memory.ShortTermMemoryNode;
import com.shiyu.ai.agent.langgraph4j.node.output.OutputFormatConfig;
import com.shiyu.ai.agent.langgraph4j.node.output.OutputFormatNode;
import com.shiyu.ai.agent.langgraph4j.node.rag.RagEnhancementConfig;
import com.shiyu.ai.agent.langgraph4j.node.rag.RagEnhancementNode;
import com.shiyu.ai.agent.langgraph4j.node.rag.RagRetrievalConfig;
import com.shiyu.ai.agent.langgraph4j.node.rag.RagRetrievalNode;
import com.shiyu.ai.agent.langgraph4j.node.tool.ToolCallConfig;
import com.shiyu.ai.agent.langgraph4j.node.tool.ToolCallNode;
import com.shiyu.ai.agent.langgraph4j.node.transform.TransformConfig;
import com.shiyu.ai.agent.langgraph4j.node.transform.TransformNode;
import com.shiyu.ai.agent.biz.agent.service.AgentService;
import com.shiyu.ai.agent.biz.agent.service.IntentService;
import com.shiyu.ai.agent.biz.agent.service.Lc4jService;
import com.shiyu.ai.agent.biz.agent.service.RagService;
import com.shiyu.ai.agent.biz.agent.service.ToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点工厂类
 * 用于根据 NodeConfig 创建相应的 Node 实例，并支持服务注册
 *
 * @author shiyu-ai
 * @date 2026-03-27
 */
@Slf4j
@Component
public class NodeFactory {

    /**
     * 节点类型映射表
     * key: 节点类型枚举
     * value: 节点创建器
     */
    private final Map<NodeType, NodeCreatorInfo> nodeCreators = new ConcurrentHashMap<>();

    /**
     * 已注册的节点实例 id -> 节点实例
     */
    private final Map<String, BaseNode> registeredNodes = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private IntentService intentService;

    @Autowired(required = false)
    private RagService ragService;

    @Autowired(required = false)
    private Lc4jService lc4jService;

    @Autowired(required = false)
    private ToolService toolService;

    @Autowired(required = false)
    private AgentService agentService;

    public NodeFactory() {
        // 注册默认节点类型
        registerDefaultNodeTypes();
    }

    /**
     * 注册默认的节点类型
     */
    private void registerDefaultNodeTypes() {
        // 注册默认节点
        registerNodeType(NodeType.DEFAULT, NodeConfig.class, config -> DefaultNode.builder().config(config).build());

        // 注册意图识别节点（使用特殊处理，需要依赖注入）
        // IntentNode 需要通过 Spring 容器获取 IntentService
        
        // 注册 RAG 相关节点（使用特殊处理，需要依赖注入）
        // RagRetrievalNode 需要通过 Spring 容器获取 RagService
        registerNodeType(NodeType.RAG_ENHANCEMENT, RagEnhancementConfig.class, config -> RagEnhancementNode.builder().config(config).build());
        
        // 注册记忆相关节点
        registerNodeType(NodeType.MEMORY_SHORT_TERM, ShortTermMemoryConfig.class, config -> ShortTermMemoryNode.builder().config(config).build());
        registerNodeType(NodeType.MEMORY_LONG_TERM, LongTermMemoryConfig.class, config -> LongTermMemoryNode.builder().config(config).build());
        registerNodeType(NodeType.MEMORY_RETRIEVAL, MemoryRetrievalConfig.class, config -> MemoryRetrievalNode.builder().config(config).build());
        
        // 注册 LLM 调用节点（使用特殊处理，需要依赖注入）
        // LlmCallNode 需要通过 Spring 容器获取 Lc4jService
        
        // 注册工具调用节点（使用特殊处理，需要依赖注入）
        // ToolCallNode 需要通过 Spring 容器获取 ToolService
        
        // 注册条件判断节点
        registerNodeType(NodeType.CONDITION, ConditionConfig.class, config -> ConditionNode.builder().config(config).build());
        
        // 注册数据转换节点
        registerNodeType(NodeType.TRANSFORM, TransformConfig.class, config -> TransformNode.builder().config(config).build());
        
        // 注册输出格式化节点
        registerNodeType(NodeType.OUTPUT_FORMAT, OutputFormatConfig.class, config -> OutputFormatNode.builder().config(config).build());
        
        // 注册 Agent 调用节点（使用特殊处理，需要依赖注入）
        // AgentCallNode 需要通过 Spring 容器获取 AgentService
        registerNodeType(NodeType.AGENT_CALL, AgentCallConfig.class, config -> AgentCallNode.builder().config(config).agentService(agentService).build());
    }

    /**
     * 注册节点类型
     *
     * @param nodeType       节点类型
     * @param configClass    配置类
     * @param nodeCreator    节点创建器
     * @param <T>            配置类型
     */
    public <T extends NodeConfig> void registerNodeType(
            NodeType nodeType,
            Class<T> configClass,
            NodeCreator<T> nodeCreator
    ) {
        nodeCreators.put(nodeType, new NodeCreatorInfo<>(configClass, nodeCreator));
        log.info("已注册节点类型：{} ({})", nodeType.getCode(), nodeType.getName());
    }

    /**
         * 节点创建器信息类
         */
        private record NodeCreatorInfo<T extends NodeConfig>(Class<T> configClass, NodeCreator<T> nodeCreator) {
    }

    /**
     * 创建需要依赖注入的节点实例
     *
     * @param nodeType 节点类型
     * @param config   节点配置
     * @return 节点实例，如果不需要依赖注入则返回 null
     */
    private BaseNode createNodeWithDependencies(NodeType nodeType, NodeConfig config) {
        return switch (nodeType) {
            case INTENT -> {
                if (intentService == null) {
                    log.warn("IntentService 未注入，无法创建 IntentNode");
                    throw new IllegalStateException("创建意图节点失败：IntentService 未注入");
                }
                yield IntentNode.builder()
                        .config((IntentConfig) config)
                        .intentService(intentService)
                        .build();
            }
            case RAG_RETRIEVAL -> {
                if (ragService == null) {
                    log.warn("RagService 未注入，无法创建 RagRetrievalNode");
                    throw new IllegalStateException("创建 RAG 检索节点失败：RagService 未注入");
                }
                yield RagRetrievalNode.builder()
                        .config((RagRetrievalConfig) config)
                        .ragService(ragService)
                        .build();
            }
            case LLM_CALL -> {
                if (lc4jService == null) {
                    log.warn("Lc4jService 未注入，无法创建 LlmCallNode");
                    throw new IllegalStateException("创建 LLM 调用节点失败：Lc4jService 未注入");
                }
                yield LlmCallNode.builder()
                        .config((LlmCallConfig) config)
                        .lc4jService(lc4jService)
                        .build();
            }
            case TOOL_CALL -> {
                if (toolService == null) {
                    log.warn("ToolService 未注入，无法创建 ToolCallNode");
                    throw new IllegalStateException("创建工具调用节点失败：ToolService 未注入");
                }
                yield ToolCallNode.builder()
                        .config((ToolCallConfig) config)
                        .toolService(toolService)
                        .build();
            }
            case AGENT_CALL -> {
                if (agentService == null) {
                    log.warn("AgentService 未注入，无法创建 AgentCallNode");
                    throw new IllegalStateException("创建 Agent 调用节点失败：AgentService 未注入");
                }
                yield AgentCallNode.builder()
                        .config((AgentCallConfig) config)
                        .agentService(agentService)
                        .build();
            }
            default -> null;
        };
    }

    /**
     * 根据配置创建节点实例
     *
     * @param config 节点配置
     * @return 节点实例
     */
    @SuppressWarnings("unchecked")
    public BaseNode createNode(NodeConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("节点配置不能为空");
        }

        NodeType nodeType = config.getNodeType();
        if (nodeType == null) {
            throw new IllegalArgumentException("节点类型不能为空");
        }

        NodeCreatorInfo<?> creatorInfo = nodeCreators.get(nodeType);
        if (creatorInfo == null) {
            throw new IllegalArgumentException("不支持的节点类型：" + nodeType.getName());
        }

        try {
            // 检查配置类型是否匹配
            if (!creatorInfo.configClass.isInstance(config)) {
                // 尝试转换配置
                config = convertConfig(config, creatorInfo.configClass);
            }

            // 对于需要依赖注入的节点类型，使用特殊方式创建
            BaseNode node = createNodeWithDependencies(nodeType, config);
            
            // 如果返回 null，说明是普通节点，使用创建器创建
            if (node == null) {
                node = ((NodeCreatorInfo<NodeConfig>) creatorInfo).nodeCreator.create(config);
            }

            // 确保节点配置已设置
            if (node.getConfig() == null) {
                node.setConfig(config);
            }

            // 注册节点实例
            if (config.getNodeId() != null && !config.getNodeId().isEmpty()) {
                registeredNodes.put(config.getNodeId(), node);
                log.debug("节点已注册：{} (ID: {})", nodeType.getName(), config.getNodeId());
            }

            log.info("成功创建节点：{} (类型：{}, ID: {})", 
                    node.getClass().getSimpleName(), nodeType.getName(), config.getNodeId());
            return node;

        } catch (Exception e) {
            log.error("创建节点失败：{} (类型：{})", config.getNodeId(), nodeType.getName(), e);
            throw new RuntimeException("创建节点失败：" + config.getNodeId(), e);
        }
    }

    /**
     * 转换配置类型
     *
     * @param sourceConfig 源配置
     * @param targetClass  目标配置类
     * @return 转换后的配置
     */
    @SuppressWarnings("unchecked")
    private <T extends NodeConfig> T convertConfig(NodeConfig sourceConfig, Class<T> targetClass) {
        try {
            // 如果已经是目标类型，直接返回
            if (targetClass.isInstance(sourceConfig)) {
                return (T) sourceConfig;
            }

            // 创建新的配置实例
            T targetConfig = targetClass.getDeclaredConstructor().newInstance();

            // 复制公共属性
            copyProperties(sourceConfig, targetConfig);

            log.debug("配置类型已转换：{} -> {}", 
                    sourceConfig.getClass().getSimpleName(), targetClass.getSimpleName());
            return targetConfig;

        } catch (Exception e) {
            log.error("配置转换失败：{} -> {}", 
                    sourceConfig.getClass().getSimpleName(), targetClass.getSimpleName(), e);
            throw new RuntimeException("配置转换失败", e);
        }
    }

    /**
     * 复制对象属性（支持 null 跳过、final 字段兼容、深拷贝 Map/List）
     *
     * @param source 源对象
     * @param target 目标对象
     */
    private void copyProperties(Object source, Object target) {
        var sourceFields = source.getClass().getDeclaredFields();
        for (var sourceField : sourceFields) {
            try {
                var fieldName = sourceField.getName();

                // 跳过 static 字段
                if (java.lang.reflect.Modifier.isStatic(sourceField.getModifiers())) {
                    continue;
                }

                sourceField.setAccessible(true);
                var value = sourceField.get(source);

                var targetField = target.getClass().getDeclaredField(fieldName);

                // 跳过 final 字段（@Builder.Default 通过 final 实现）
                if (java.lang.reflect.Modifier.isFinal(targetField.getModifiers())) {
                    continue;
                }

                targetField.setAccessible(true);

                // 空值不覆盖目标默认值
                if (value == null) {
                    continue;
                }

                // 深拷贝 Map / List
                if (value instanceof Map<?, ?> map) {
                    targetField.set(target, new HashMap<>(map));
                } else if (value instanceof List<?> list) {
                    targetField.set(target, new ArrayList<>(list));
                } else {
                    targetField.set(target, value);
                }

            } catch (NoSuchFieldException e) {
                // 忽略目标类没有的字段
            } catch (Exception e) {
                log.warn("属性复制失败：{} -> {}.{}", sourceField.getName(), target.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    /**
     * 根据节点 ID 获取已注册的节点
     *
     * @param nodeId 节点 ID
     * @return 节点实例，不存在则返回 null
     */
    public BaseNode getNode(String nodeId) {
        return registeredNodes.get(nodeId);
    }

    /**
     * 移除已注册的节点
     *
     * @param nodeId 节点 ID
     * @return 是否移除成功
     */
    public boolean removeNode(String nodeId) {
        BaseNode removedNode = registeredNodes.remove(nodeId);
        if (removedNode != null) {
            log.debug("节点已移除：{}", nodeId);
            return true;
        }
        return false;
    }

    /**
     * 清空所有已注册的节点
     */
    public void clearNodes() {
        registeredNodes.clear();
        log.info("已清空所有已注册的节点");
    }

    /**
     * 注册服务到节点
     * 通过反射将服务实例注入到节点实例的对应字段中
     *
     * @param nodeId      节点 ID
     * @param serviceName 服务名称（对应节点类中的字段名）
     * @param service     服务实例
     */
    public void registerServiceToNode(String nodeId, String serviceName, Object service) {
        BaseNode node = getNode(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("节点不存在：" + nodeId);
        }

        // 尝试字段注入（字段名匹配或类型匹配）
        Class<?> nodeClass = node.getClass();
        boolean injected = false;

        // 1. 按字段名精确匹配
        try {
            java.lang.reflect.Field field = nodeClass.getDeclaredField(serviceName);
            field.setAccessible(true);
            // 检查类型兼容
            if (field.getType().isInstance(service)) {
                field.set(node, service);
                injected = true;
            }
        } catch (NoSuchFieldException ignored) {
            // 字段名不匹配，尝试类型匹配
        } catch (Exception e) {
            log.warn("服务字段注入失败：{}.{}", nodeId, serviceName, e);
        }

        // 2. 按类型匹配注入（从父类开始搜索）
        if (!injected) {
            for (Class<?> cls = nodeClass; cls != null && cls != Object.class; cls = cls.getSuperclass()) {
                for (var field : cls.getDeclaredFields()) {
                    if (field.getType().isInstance(service)) {
                        try {
                            field.setAccessible(true);
                            field.set(node, service);
                            injected = true;
                            log.debug("按类型匹配注入服务：{} -> {}.{}", serviceName, nodeId, field.getName());
                            break;
                        } catch (Exception e) {
                            log.warn("按类型匹配注入失败", e);
                        }
                    }
                }
                if (injected) break;
            }
        }

        if (injected) {
            log.info("服务已注册到节点：{} (服务名：{})", nodeId, serviceName);
        } else {
            log.warn("服务注册到节点失败，未找到匹配的字段：{} (服务名：{}, 服务类型：{})",
                    nodeId, serviceName, service.getClass().getSimpleName());
        }
    }

    /**
     * 批量创建节点
     *
     * @param configs 节点配置列表
     * @return 创建的节点实例列表
     * @throws RuntimeException 当创建任何一个节点失败时，抛出包含所有失败原因的异常
     */
    public Map<String, BaseNode> createNodes(Map<String, NodeConfig> configs) {
        Map<String, BaseNode> nodes = new HashMap<>();
        List<String> errors = new ArrayList<>();
        for (NodeConfig config : configs.values()) {
            try {
                BaseNode node = createNode(config);
                nodes.put(config.getNodeId(), node);
            } catch (Exception e) {
                log.error("创建节点失败：{}", config.getNodeId(), e);
                errors.add(config.getNodeId() + ": " + e.getMessage());
            }
        }
        if (!errors.isEmpty()) {
            throw new RuntimeException("部分节点创建失败：" + String.join("; ", errors));
        }
        return nodes;
    }
    
    /**
     * 批量创建节点并注册服务
     *
     * @param configs          节点配置列表
     * @param serviceProviders 服务提供者映射（节点 ID -> 服务名 -> 服务实例）
     * @return 创建的节点实例列表
     */
    public Map<String, BaseNode> createNodesWithServices(
            Map<String, NodeConfig> configs,
            Map<String, Map<String, Object>> serviceProviders
    ) {
        Map<String, BaseNode> nodes = createNodes(configs);
        
        // 为每个节点注册对应的服务
        for (Map.Entry<String, Map<String, Object>> entry : serviceProviders.entrySet()) {
            String nodeId = entry.getKey();
            Map<String, Object> services = entry.getValue();
            
            for (Map.Entry<String, Object> serviceEntry : services.entrySet()) {
                registerServiceToNode(nodeId, serviceEntry.getKey(), serviceEntry.getValue());
            }
        }
        
        return nodes;
    }

    /**
     * 获取所有已注册的节点
     *
     * @return 已注册的节点映射
     */
    public Map<String, BaseNode> getAllRegisteredNodes() {
        return new HashMap<>(registeredNodes);
    }

    /**
     * 根据节点类型和配置创建节点
     * 便捷方法，自动创建对应的 Config
     *
     * @param nodeType   节点类型
     * @param nodeId     节点 ID
     * @param nodeName   节点名称
     * @param initializer 配置初始化器
     * @return 节点实例
     */
    public BaseNode createNode(NodeType nodeType, String nodeId, String nodeName,
                               java.util.function.Consumer<NodeConfig> initializer) {
        NodeCreatorInfo<?> creatorInfo = nodeCreators.get(nodeType);
        if (creatorInfo == null) {
            throw new IllegalArgumentException("不支持的节点类型：" + nodeType.getName());
        }

        try {
            // 创建对应的 Config 实例
            NodeConfig config = creatorInfo.configClass.getDeclaredConstructor().newInstance();
            config.setNodeId(nodeId);
            config.setNodeName(nodeName);
            config.setNodeType(nodeType);

            // 应用初始化器
            if (initializer != null) {
                initializer.accept(config);
            }

            return createNode(config);

        } catch (Exception e) {
            log.error("创建节点失败：{} (类型：{})", nodeId, nodeType.getName(), e);
            throw new RuntimeException("创建节点失败：" + nodeId, e);
        }
    }

    /**
     * 根据节点类型创建节点（简化版）
     * 使用默认配置创建节点
     *
     * @param nodeType   节点类型
     * @param nodeId     节点 ID
     * @param nodeName   节点名称
     * @return 节点实例
     */
    public BaseNode createNode(NodeType nodeType, String nodeId, String nodeName) {
        return createNode(nodeType, nodeId, nodeName, null);
    }

    /**
     * 节点创建器接口
     *
     * @param <T> 配置类型
     */
    @FunctionalInterface
    public interface NodeCreator<T extends NodeConfig> {
        BaseNode create(T config);
    }
}
