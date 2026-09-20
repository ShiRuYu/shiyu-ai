package com.shiyu.ai.agent.implementation.node.creator;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.agent.implementation.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.implementation.node.llm.LlmCallNode;
import com.shiyu.ai.model.contract.api.ChatEngine;

import org.springframework.stereotype.Component;

/**
 * 根据输入配置创建 Llm Call Node 相关的流程节点或业务组件。
 */
@Component
public class LlmCallNodeCreator implements NodeCreator {
    /**
     * chatEngine 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ChatEngine chatEngine;

    /**
     * 执行 Llm Call Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param chatEngine 用于完成本次业务处理的 chatEngine 参数。
     */
    public LlmCallNodeCreator(ChatEngine chatEngine) {
        this.chatEngine = chatEngine;
    }

    /**
     * 查询 Llm Call Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Llm Call Node 相关操作生成的结果数据。
     */
    @Override
    public NodeType getType() {
        return NodeType.LLM_CALL;
    }

    /**
     * 创建或保存 Llm Call Node 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回 Llm Call Node 相关操作生成的结果数据。
     */
    @Override
    public BaseNode create(NodeConfig config) {
        return LlmCallNode.builder().config((LlmCallConfig) config).chatEngine(chatEngine).build();
    }
}
