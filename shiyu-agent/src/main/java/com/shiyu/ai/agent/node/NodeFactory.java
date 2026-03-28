package com.shiyu.ai.agent.node;

import com.shiyu.ai.agent.node.condition.ConditionConfig;
import com.shiyu.ai.agent.node.condition.ConditionNode;
import com.shiyu.ai.agent.node.intent.IntentConfig;
import com.shiyu.ai.agent.node.intent.IntentNode;
import com.shiyu.ai.agent.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.node.llm.LlmCallNode;
import com.shiyu.ai.agent.node.memory.LongTermMemoryConfig;
import com.shiyu.ai.agent.node.memory.LongTermMemoryNode;
import com.shiyu.ai.agent.node.memory.MemoryRetrievalConfig;
import com.shiyu.ai.agent.node.memory.MemoryRetrievalNode;
import com.shiyu.ai.agent.node.memory.ShortTermMemoryConfig;
import com.shiyu.ai.agent.node.memory.ShortTermMemoryNode;
import com.shiyu.ai.agent.node.output.OutputFormatConfig;
import com.shiyu.ai.agent.node.output.OutputFormatNode;
import com.shiyu.ai.agent.node.rag.RagEnhancementConfig;
import com.shiyu.ai.agent.node.rag.RagEnhancementNode;
import com.shiyu.ai.agent.node.rag.RagRetrievalConfig;
import com.shiyu.ai.agent.node.rag.RagRetrievalNode;
import com.shiyu.ai.agent.node.tool.ToolCallConfig;
import com.shiyu.ai.agent.node.tool.ToolCallNode;
import com.shiyu.ai.agent.node.transform.TransformConfig;
import com.shiyu.ai.agent.node.transform.TransformNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public NodeFactory() {
        // 注册默认节点类型
        registerDefaultNodeTypes();
    }

    /**
     * 注册默认的节点类型
     */
    private void registerDefaultNodeTypes() {
        // 注册默认节点
        registerNodeType(NodeType.DEFAULT, NodeConfig.class, DefaultNode::new);

        // 注册意图识别节点
        registerNodeType(NodeType.INTENT, IntentConfig.class, IntentNode::new);
        
        // 注册 RAG 相关节点
        registerNodeType(NodeType.RAG_RETRIEVAL, RagRetrievalConfig.class, RagRetrievalNode::new);
        registerNodeType(NodeType.RAG_ENHANCEMENT, RagEnhancementConfig.class, RagEnhancementNode::new);
        
        // 注册记忆相关节点
        registerNodeType(NodeType.MEMORY_SHORT_TERM, ShortTermMemoryConfig.class, ShortTermMemoryNode::new);
        registerNodeType(NodeType.MEMORY_LONG_TERM, LongTermMemoryConfig.class, LongTermMemoryNode::new);
        registerNodeType(NodeType.MEMORY_RETRIEVAL, MemoryRetrievalConfig.class, MemoryRetrievalNode::new);
        
        // 注册 LLM 调用节点
        registerNodeType(NodeType.LLM_CALL, LlmCallConfig.class, LlmCallNode::new);
        
        // 注册工具调用节点
        registerNodeType(NodeType.TOOL_CALL, ToolCallConfig.class, ToolCallNode::new);
        
        // 注册条件判断节点
        registerNodeType(NodeType.CONDITION, ConditionConfig.class, ConditionNode::new);
        
        // 注册数据转换节点
        registerNodeType(NodeType.TRANSFORM, TransformConfig.class, TransformNode::new);
        
        // 注册输出格式化节点
        registerNodeType(NodeType.OUTPUT_FORMAT, OutputFormatConfig.class, OutputFormatNode::new);
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

            // 使用对应的创建器创建节点
            BaseNode node = ((NodeCreatorInfo<NodeConfig>) creatorInfo).nodeCreator.create(config);

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
     * 复制对象属性
     *
     * @param source 源对象
     * @param target 目标对象
     */
    private void copyProperties(Object source, Object target) {
        try {
            var sourceFields = source.getClass().getDeclaredFields();
            for (var sourceField : sourceFields) {
                try {
                    var fieldName = sourceField.getName();
                    sourceField.setAccessible(true);
                    var value = sourceField.get(source);

                    var targetField = target.getClass().getDeclaredField(fieldName);
                    if (targetField != null) {
                        targetField.setAccessible(true);
                        targetField.set(target, value);
                    }
                } catch (NoSuchFieldException e) {
                    // 忽略目标类没有的字段
                }
            }
        } catch (Exception e) {
            log.warn("属性复制失败", e);
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
     * 允许将外部服务注入到节点实例中
     *
     * @param nodeId      节点 ID
     * @param serviceName 服务名称
     * @param service     服务实例
     */
    public void registerServiceToNode(String nodeId, String serviceName, Object service) {
        BaseNode node = getNode(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("节点不存在：" + nodeId);
        }

        // 如果节点是 IntentNode 类型，可以注册特定的服务
        if (node instanceof IntentNode intentNode) {
            if ("INTENT_RECOGNITION_SERVICE".equals(serviceName)) {
                intentNode.registerIntentRecognitionService(service);
            }
        }

        log.info("服务已注册到节点：{} (服务名：{})", nodeId, serviceName);
    }

    /**
     * 注册意图节点相关的服务
     *
     * @param intentNode  意图节点
     * @param serviceName 服务名称
     * @param service     服务实例
     */
    private void registerIntentServices(IntentNode intentNode, String serviceName, Object service) {
        // 这里可以根据 serviceName 注册不同的服务
        // 例如：intentNode.setIntentRecognitionService((IntentRecognitionService) service);
        log.debug("为意图节点注册服务：{}", serviceName);
    }

    /**
     * 批量创建节点
     *
     * @param configs 节点配置列表
     * @return 创建的节点实例列表
     */
    public Map<String, BaseNode> createNodes(Map<String, NodeConfig> configs) {
        Map<String, BaseNode> nodes = new HashMap<>();
        for (NodeConfig config : configs.values()) {
            try {
                BaseNode node = createNode(config);
                nodes.put(config.getNodeId(), node);
            } catch (Exception e) {
                log.error("创建节点失败：{}", config.getNodeId(), e);
            }
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
