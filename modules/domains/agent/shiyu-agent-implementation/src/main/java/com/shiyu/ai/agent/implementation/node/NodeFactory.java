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
 * Node creation facade. Type registration, instance construction, and runtime
 * service injection are separate collaborators so this class only coordinates
 * the public factory API.
 */
@Slf4j
@Component
@SuppressWarnings("this-escape")
public class NodeFactory {
    private final NodeTypeRegistry registry;
    private final NodeInstanceFactory instanceFactory;
    private final RegisteredNodeStore nodeStore;

    public NodeFactory(List<com.shiyu.ai.agent.contract.node.creator.NodeCreator> beanNodeCreators,
                       ExecutionHistoryService executionHistoryService) {
        this.registry = new NodeTypeRegistry();
        this.instanceFactory = new NodeInstanceFactory(registry, beanNodeCreators, executionHistoryService);
        this.nodeStore = new RegisteredNodeStore();
    }

    public <T extends NodeConfig> void registerNodeType(NodeType nodeType, Class<T> configClass,
                                                         NodeCreator<T> nodeCreator) {
        registry.register(nodeType, configClass, nodeCreator);
    }

    public BaseNode createNode(NodeConfig config) {
        BaseNode node = instanceFactory.create(config);
        if (config.getNodeId() != null && !config.getNodeId().isEmpty()) {
            nodeStore.put(config.getNodeId(), node);
        }
        return node;
    }

    public BaseNode getNode(String nodeId) {
        return nodeStore.get(nodeId);
    }

    public boolean removeNode(String nodeId) {
        boolean removed = nodeStore.remove(nodeId);
        if (removed) {
            log.debug("节点已移除：nodeIdPresent={}", nodeId != null);
        }
        return removed;
    }

    public void clearNodes() {
        nodeStore.clear();
        log.info("已清空所有已注册的节点");
    }

    public void registerServiceToNode(String nodeId, String serviceName, Object service) {
        nodeStore.inject(nodeId, serviceName, service);
    }

    public Map<String, BaseNode> createNodes(Map<String, NodeConfig> configs) {
        Map<String, BaseNode> nodes = new HashMap<>();
        List<String> errors = new ArrayList<>();
        for (NodeConfig config : configs.values()) {
            try {
                nodes.put(config.getNodeId(), createNode(config));
            } catch (Exception exception) {
                log.error("创建节点失败：nodeIdPresent={}, errorType={}, errorMessageLength={}",
                        config.getNodeId() != null, exception.getClass().getSimpleName(),
                        messageLength(exception));
                errors.add(config.getNodeId() + ": " + exception.getMessage());
            }
        }
        if (!errors.isEmpty()) {
            throw new RuntimeException("部分节点创建失败：" + String.join("; ", errors));
        }
        return nodes;
    }

    public Map<String, BaseNode> createNodesWithServices(
            Map<String, NodeConfig> configs,
            Map<String, Map<String, Object>> serviceProviders) {
        Map<String, BaseNode> nodes = createNodes(configs);
        for (Map.Entry<String, Map<String, Object>> entry : serviceProviders.entrySet()) {
            for (Map.Entry<String, Object> service : entry.getValue().entrySet()) {
                registerServiceToNode(entry.getKey(), service.getKey(), service.getValue());
            }
        }
        return nodes;
    }

    public Map<String, BaseNode> getAllRegisteredNodes() {
        return nodeStore.snapshot();
    }

    public BaseNode createNode(NodeType nodeType, String nodeId, String nodeName,
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
            log.error("创建节点失败：nodeIdPresent={}, nodeTypePresent={}, errorType={}, errorMessageLength={}",
                    nodeId != null, nodeType != null && nodeType.getName() != null,
                    exception.getClass().getSimpleName(), messageLength(exception));
            throw new RuntimeException("创建节点失败：" + nodeId, exception);
        }
    }

    public BaseNode createNode(NodeType nodeType, String nodeId, String nodeName) {
        return createNode(nodeType, nodeId, nodeName, null);
    }

    private static int messageLength(Exception exception) {
        return exception.getMessage() == null ? 0 : exception.getMessage().length();
    }

    @FunctionalInterface
    public interface NodeCreator<T extends NodeConfig> {
        BaseNode create(T config);
    }
}
