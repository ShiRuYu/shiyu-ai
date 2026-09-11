package com.shiyu.ai.agent.implementation.node;

import cn.hutool.core.bean.BeanUtil;

import com.shiyu.ai.agent.contract.ExecutionHistoryService;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/** Creates node instances, including Spring creator adapters and config conversion. */
@Slf4j
final class NodeInstanceFactory {
    private final NodeTypeRegistry registry;
    private final List<NodeCreator> beanNodeCreators;
    private final ExecutionHistoryService executionHistoryService;

    NodeInstanceFactory(
            NodeTypeRegistry registry,
            List<com.shiyu.ai.agent.contract.node.creator.NodeCreator> beanNodeCreators,
            ExecutionHistoryService executionHistoryService) {
        this.registry = registry;
        this.beanNodeCreators = beanNodeCreators;
        this.executionHistoryService = executionHistoryService;
    }

    BaseNode create(NodeConfig sourceConfig) {
        if (sourceConfig == null) {
            throw new IllegalArgumentException("节点配置不能为空");
        }
        NodeType nodeType = sourceConfig.getNodeType();
        if (nodeType == null) {
            throw new IllegalArgumentException("节点类型不能为空");
        }
        NodeTypeRegistry.CreatorInfo<?> creatorInfo = registry.get(nodeType);
        if (creatorInfo == null) {
            throw new IllegalArgumentException("不支持的节点类型：" + nodeType.getName());
        }
        try {
            NodeConfig config = sourceConfig;
            if (!creatorInfo.configClass().isInstance(config)) {
                config = convertConfig(config, creatorInfo.configClass());
            }
            BaseNode node = createFromBean(nodeType, config);
            if (node == null) {
                node = createFromFallback(creatorInfo, config);
            }
            if (node.getConfig() == null) {
                node.setConfig(config);
            }
            if (executionHistoryService != null) {
                node.setExecutionHistoryService(executionHistoryService);
            }
            log.info(
                    "成功创建节点：classPresent={}, nodeTypePresent={}, nodeIdPresent={}",
                    node.getClass().getSimpleName() != null,
                    nodeType.getName() != null,
                    config.getNodeId() != null);
            return node;
        } catch (Exception e) {
            log.error(
                    "创建节点失败：nodeIdPresent={}, nodeTypePresent={}, errorType={},"
                            + " errorMessageLength={}",
                    sourceConfig.getNodeId() != null,
                    nodeType.getName() != null,
                    e.getClass().getSimpleName(),
                    messageLength(e));
            throw new RuntimeException("创建节点失败：" + sourceConfig.getNodeId(), e);
        }
    }

    private BaseNode createFromBean(NodeType nodeType, NodeConfig config) {
        if (beanNodeCreators == null || beanNodeCreators.isEmpty()) {
            return null;
        }
        for (NodeCreator creator : beanNodeCreators) {
            if (creator.getType() == nodeType) {
                try {
                    return creator.create(config);
                } catch (Exception e) {
                    log.error(
                            "节点创建失败: typePresent={}, errorType={}, errorMessageLength={}",
                            nodeType != null,
                            e.getClass().getSimpleName(),
                            messageLength(e));
                    return null;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private BaseNode createFromFallback(
            NodeTypeRegistry.CreatorInfo<?> creatorInfo, NodeConfig config) {
        return ((NodeFactory.NodeCreator<NodeConfig>) creatorInfo.nodeCreator()).create(config);
    }

    @SuppressWarnings("unchecked")
    private <T extends NodeConfig> T convertConfig(NodeConfig sourceConfig, Class<T> targetClass) {
        try {
            T targetConfig = targetClass.getDeclaredConstructor().newInstance();
            BeanUtil.copyProperties(
                    sourceConfig,
                    targetConfig,
                    cn.hutool.core.bean.copier.CopyOptions.create()
                            .ignoreNullValue()
                            .ignoreError());
            return targetConfig;
        } catch (Exception e) {
            throw new RuntimeException("配置转换失败", e);
        }
    }

    private static int messageLength(Exception exception) {
        return exception.getMessage() == null ? 0 : exception.getMessage().length();
    }
}
