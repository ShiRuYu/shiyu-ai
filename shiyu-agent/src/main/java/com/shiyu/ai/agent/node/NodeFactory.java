package com.shiyu.ai.agent.node;

import com.shiyu.ai.agent.node.intent.IntentConfig;
import com.shiyu.ai.agent.node.intent.IntentNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
     * value: 节点创建函数
     */
    private final Map<NodeType, Function<NodeConfig, BaseNode>> nodeCreators;

    /**
     * 已注册的节点实例 id -> 节点实例
     */
    private final Map<String, BaseNode> registeredNodes;

    public NodeFactory() {
        this.nodeCreators = new HashMap<>();
        this.registeredNodes = new HashMap<>();
        // 注册默认节点类型
        registerDefaultNodeTypes();
    }

    /**
     * 注册默认的节点类型
     */
    private void registerDefaultNodeTypes() {
        // 注册意图识别节点
        registerNodeType(NodeType.INTENT, IntentConfig.class::cast, IntentNode::new);
    }

    /**
     * 注册节点类型
     *
     * @param nodeType       节点类型
     * @param configConverter 配置转换器
     * @param nodeCreator    节点创建器
     * @param <T>            配置类型
     */
    public <T extends NodeConfig> void registerNodeType(
            NodeType nodeType,
            Function<NodeConfig, T> configConverter,
            NodeCreator<T> nodeCreator
    ) {
        nodeCreators.put(nodeType, config -> {
            try {
                T convertedConfig = configConverter.apply(config);
                BaseNode node = nodeCreator.create(convertedConfig);
                log.info("成功创建节点：{} (类型：{})", config.getNodeId(), nodeType.getName());
                return node;
            } catch (Exception e) {
                log.error("创建节点失败：{} (类型：{})", config.getNodeId(), nodeType.getName(), e);
                throw new RuntimeException("创建节点失败：" + config.getNodeId(), e);
            }
        });
    }

    /**
     * 根据配置创建节点实例
     *
     * @param config 节点配置
     * @return 节点实例
     */
    public BaseNode createNode(NodeConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("节点配置不能为空");
        }

        NodeType nodeType = config.getNodeType();
        if (nodeType == null) {
            throw new IllegalArgumentException("节点类型不能为空");
        }

        Function<NodeConfig, BaseNode> creator = nodeCreators.get(nodeType);
        if (creator == null) {
            throw new IllegalArgumentException("不支持的节点类型：" + nodeType.getName());
        }

        BaseNode node = creator.apply(config);
        
        // 设置配置到节点
        node.setConfig(config);

        // 注册节点实例
        if (config.getNodeId() != null && !config.getNodeId().isEmpty()) {
            registeredNodes.put(config.getNodeId(), node);
            log.debug("节点已注册：{} (ID: {})", nodeType.getName(), config.getNodeId());
        }

        return node;
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
     * 节点创建器接口
     *
     * @param <T> 配置类型
     */
    @FunctionalInterface
    public interface NodeCreator<T extends NodeConfig> {
        BaseNode create(T config);
    }
}
