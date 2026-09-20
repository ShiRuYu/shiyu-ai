package com.shiyu.ai.agent.implementation.node.creator;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.agent.implementation.node.memory.ShortTermMemoryConfig;
import com.shiyu.ai.agent.implementation.node.memory.ShortTermMemoryNode;
import com.shiyu.ai.agent.implementation.runtime.model.AgentExecutionContext;

import org.springframework.stereotype.Component;

/**
 * 根据输入配置创建 Short Term 记忆 Node 相关的流程节点或业务组件。
 */
@Component
public class ShortTermMemoryNodeCreator implements NodeCreator {
    /**
     * memoryService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentExecutionContext memoryService;

    /**
     * 执行 Short Term 记忆 Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param memoryService 用于完成本次业务处理的 memoryService 参数。
     */
    public ShortTermMemoryNodeCreator(AgentExecutionContext memoryService) {
        this.memoryService = memoryService;
    }

    /**
     * 查询 Short Term 记忆 Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Short Term 记忆 Node 相关操作生成的结果数据。
     */
    @Override
    public NodeType getType() {
        return NodeType.MEMORY_SHORT_TERM;
    }

    /**
     * 创建或保存 Short Term 记忆 Node 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回 Short Term 记忆 Node 相关操作生成的结果数据。
     */
    @Override
    public BaseNode create(NodeConfig config) {
        return ShortTermMemoryNode.builder()
                .config((ShortTermMemoryConfig) config)
                .memoryService(memoryService)
                .build();
    }
}
