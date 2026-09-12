package com.shiyu.ai.agent.implementation.node;

import com.shiyu.ai.agent.contract.ExecutionHistoryService;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据节点类型创建可执行节点并注入运行时依赖。
 */
@Slf4j
@Component
@SuppressWarnings("this-escape")
public class NodeFactory {
    /**
     * registry 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final NodeTypeRegistry registry;
    /**
     * instanceFactory 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final NodeInstanceFactory instanceFactory;
    /**
     * nodeStore 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final RegisteredNodeStore nodeStore;

    /**
     * {@code NodeFactory} 创建并初始化当前类型实例。
     *
     * @param beanNodeCreators 参数值，用于执行当前操作。
     * @param executionHistoryService 参数值，用于执行当前操作。
     */
    public NodeFactory(
            List<com.shiyu.ai.agent.contract.node.creator.NodeCreator> beanNodeCreators,
            ExecutionHistoryService executionHistoryService) {
        this.registry = new NodeTypeRegistry();
        this.instanceFactory =
                new NodeInstanceFactory(registry, beanNodeCreators, executionHistoryService);
        this.nodeStore = new RegisteredNodeStore();
    }

    /**
     * {@code registerNodeType} 写入或更新当前模块中的业务数据。
     *
     * @param nodeType 参数值，用于执行当前操作。
     * @param configClass 参数值，用于执行当前操作。
     * @param nodeCreator 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public <T extends NodeConfig> void registerNodeType(
            NodeType nodeType, Class<T> configClass, NodeCreator<T> nodeCreator) {
        registry.register(nodeType, configClass, nodeCreator);
    }

    /**
     * {@code createNode} 写入或更新当前模块中的业务数据。
     *
     * @param config 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public BaseNode createNode(NodeConfig config) {
        BaseNode node = instanceFactory.create(config);
        if (config.getNodeId() != null && !config.getNodeId().isEmpty()) {
            nodeStore.put(config.getNodeId(), node);
        }
        return node;
    }

    /**
     * {@code getNode} 查询并返回当前操作所需的数据。
     *
     * @param nodeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public BaseNode getNode(String nodeId) {
        return nodeStore.get(nodeId);
    }

    /**
     * {@code removeNode} 释放或移除当前操作涉及的资源。
     *
     * @param nodeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean removeNode(String nodeId) {
        boolean removed = nodeStore.remove(nodeId);
        if (removed) {
            log.debug("节点已移除：nodeIdPresent={}", nodeId != null);
        }
        return removed;
    }

    /**
     * {@code clearNodes} 执行当前类型定义的业务操作。
     */
    public void clearNodes() {
        nodeStore.clear();
        log.info("已清空所有已注册的节点");
    }

    /**
     * {@code registerServiceToNode} 写入或更新当前模块中的业务数据。
     *
     * @param nodeId 参数值，用于执行当前操作。
     * @param serviceName 参数值，用于执行当前操作。
     * @param service 参数值，用于执行当前操作。
     */
    public void registerServiceToNode(String nodeId, String serviceName, Object service) {
        nodeStore.inject(nodeId, serviceName, service);
    }

    /**
     * {@code createNodes} 写入或更新当前模块中的业务数据。
     *
     * @param configs 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, BaseNode> createNodes(Map<String, NodeConfig> configs) {
        Map<String, BaseNode> nodes = new HashMap<>();
        List<String> errors = new ArrayList<>();
        for (NodeConfig config : configs.values()) {
            try {
                nodes.put(config.getNodeId(), createNode(config));
            } catch (Exception exception) {
                log.error(
                        "创建节点失败：nodeIdPresent={}, errorType={}, errorMessageLength={}",
                        config.getNodeId() != null,
                        exception.getClass().getSimpleName(),
                        messageLength(exception));
                errors.add(config.getNodeId() + ": " + exception.getMessage());
            }
        }
        if (!errors.isEmpty()) {
            throw new RuntimeException("部分节点创建失败：" + String.join("; ", errors));
        }
        return nodes;
    }

    /**
     * {@code createNodesWithServices} 写入或更新当前模块中的业务数据。
     *
     * @param configs 参数值，用于执行当前操作。
     * @param serviceProviders 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, BaseNode> createNodesWithServices(
            Map<String, NodeConfig> configs, Map<String, Map<String, Object>> serviceProviders) {
        Map<String, BaseNode> nodes = createNodes(configs);
        for (Map.Entry<String, Map<String, Object>> entry : serviceProviders.entrySet()) {
            for (Map.Entry<String, Object> service : entry.getValue().entrySet()) {
                registerServiceToNode(entry.getKey(), service.getKey(), service.getValue());
            }
        }
        return nodes;
    }

    /**
     * {@code getAllRegisteredNodes} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, BaseNode> getAllRegisteredNodes() {
        return nodeStore.snapshot();
    }

    /**
     * {@code createNode} 写入或更新当前模块中的业务数据。
     *
     * @param nodeType 参数值，用于执行当前操作。
     * @param nodeId 参数值，用于执行当前操作。
     * @param nodeName 参数值，用于执行当前操作。
     * @param initializer 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public BaseNode createNode(
            NodeType nodeType,
            String nodeId,
            String nodeName,
            java.util.function.Consumer<NodeConfig> initializer) {
        NodeTypeRegistry.CreatorInfo<?> creatorInfo = registry.get(nodeType);
        if (creatorInfo == null) {
            throw new IllegalArgumentException("不支持的节点类型：" + nodeType.getName());
        }
        try {
            NodeConfig config = creatorInfo.configClass().getDeclaredConstructor().newInstance();
            config.setNodeId(nodeId);
            config.setNodeName(nodeName);
            config.setNodeType(nodeType);
            if (initializer != null) {
                initializer.accept(config);
            }
            return createNode(config);
        } catch (Exception exception) {
            log.error(
                    "创建节点失败：nodeIdPresent={}, nodeTypePresent={}, errorType={},"
                            + " errorMessageLength={}",
                    nodeId != null,
                    nodeType != null && nodeType.getName() != null,
                    exception.getClass().getSimpleName(),
                    messageLength(exception));
            throw new RuntimeException("创建节点失败：" + nodeId, exception);
        }
    }

    /**
     * {@code createNode} 写入或更新当前模块中的业务数据。
     *
     * @param nodeType 参数值，用于执行当前操作。
     * @param nodeId 参数值，用于执行当前操作。
     * @param nodeName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public BaseNode createNode(NodeType nodeType, String nodeId, String nodeName) {
        return createNode(nodeType, nodeId, nodeName, null);
    }

    private static int messageLength(Exception exception) {
        return exception.getMessage() == null ? 0 : exception.getMessage().length();
    }

    /**
     * NodeCreator 接口，定义智能体模块的能力边界。
     */
    @FunctionalInterface
    public interface NodeCreator<T extends NodeConfig> {
        /**
         * 创建并保存业务对象。
         *
         * @param config 配置参数。
         *
         * @return 操作结果。
         */
        BaseNode create(T config);
    }
}
