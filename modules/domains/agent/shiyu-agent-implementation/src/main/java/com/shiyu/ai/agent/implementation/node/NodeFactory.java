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
 * 创建或提供 Node 相关的业务组件和运行时能力。
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
     * 执行 Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param beanNodeCreators 用于完成本次业务处理的 beanNodeCreators 参数。
     * @param executionHistoryService 用于完成本次业务处理的 executionHistoryService 参数。
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
     * 创建或保存 Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param nodeType 用于完成本次业务处理的 nodeType 参数。
     * @param configClass 用于完成本次业务处理的 configClass 参数。
     * @param nodeCreator 用于完成本次业务处理的 nodeCreator 参数。
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public <T extends NodeConfig> void registerNodeType(
            NodeType nodeType, Class<T> configClass, NodeCreator<T> nodeCreator) {
        registry.register(nodeType, configClass, nodeCreator);
    }

    /**
     * 创建或保存 Node 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public BaseNode createNode(NodeConfig config) {
        BaseNode node = instanceFactory.create(config);
        if (config.getNodeId() != null && !config.getNodeId().isEmpty()) {
            nodeStore.put(config.getNodeId(), node);
        }
        return node;
    }

    /**
     * 查询 Node 相关业务数据，并返回处理结果。
     *
     * @param nodeId 用于定位node的标识。
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public BaseNode getNode(String nodeId) {
        return nodeStore.get(nodeId);
    }

    /**
     * 删除或移除 Node 相关业务数据，并返回处理结果。
     *
     * @param nodeId 用于定位node的标识。
     * @return 返回本次条件判断是否成立。
     */
    public boolean removeNode(String nodeId) {
        boolean removed = nodeStore.remove(nodeId);
        if (removed) {
            log.debug("节点已移除：nodeIdPresent={}", nodeId != null);
        }
        return removed;
    }

    /**
     * 删除或移除 Node 相关业务操作，并维护必要的状态和协作关系。
     */
    public void clearNodes() {
        nodeStore.clear();
        log.info("已清空所有已注册的节点");
    }

    /**
     * 创建或保存 Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param nodeId 用于定位node的标识。
     * @param serviceName 用于完成本次业务处理的 serviceName 参数。
     * @param service 用于完成本次业务处理的 service 参数。
     */
    public void registerServiceToNode(String nodeId, String serviceName, Object service) {
        nodeStore.inject(nodeId, serviceName, service);
    }

    /**
     * 创建或保存 Node 相关业务数据，并返回处理结果。
     *
     * @param configs 用于完成本次业务处理的 configs 参数。
     * @return 返回 Node 相关操作生成的结果数据。
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
     * 创建或保存 Node 相关业务数据，并返回处理结果。
     *
     * @param configs 用于完成本次业务处理的 configs 参数。
     * @param serviceProviders 用于完成本次业务处理的 serviceProviders 参数。
     * @return 返回 Node 相关操作生成的结果数据。
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
     * 查询 Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public Map<String, BaseNode> getAllRegisteredNodes() {
        return nodeStore.snapshot();
    }

    /**
     * 创建或保存 Node 相关业务数据，并返回处理结果。
     *
     * @param nodeType 用于完成本次业务处理的 nodeType 参数。
     * @param nodeId 用于定位node的标识。
     * @param nodeName 用于完成本次业务处理的 nodeName 参数。
     * @param initializer 用于完成本次业务处理的 initializer 参数。
     * @return 返回 Node 相关操作生成的结果数据。
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
     * 创建或保存 Node 相关业务数据，并返回处理结果。
     *
     * @param nodeType 用于完成本次业务处理的 nodeType 参数。
     * @param nodeId 用于定位node的标识。
     * @param nodeName 用于完成本次业务处理的 nodeName 参数。
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public BaseNode createNode(NodeType nodeType, String nodeId, String nodeName) {
        return createNode(nodeType, nodeId, nodeName, null);
    }

    private static int messageLength(Exception exception) {
        return exception.getMessage() == null ? 0 : exception.getMessage().length();
    }

    /**
     * 根据输入配置创建 Node 相关的流程节点或业务组件。
     */
    @FunctionalInterface
    public interface NodeCreator<T extends NodeConfig> {
        /**
         * 创建或保存 Node 相关业务数据，并返回处理结果。
         *
         * @param config 用于完成本次业务处理的 config 参数。
         * @return 返回 Node 相关操作生成的结果数据。
         */
        BaseNode create(T config);
    }
}
