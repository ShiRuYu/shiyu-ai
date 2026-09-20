package com.shiyu.ai.agent.implementation.node.creator;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.agent.implementation.node.tool.ToolCallConfig;
import com.shiyu.ai.agent.implementation.node.tool.ToolCallNode;
import com.shiyu.ai.agent.implementation.runtime.service.ToolExecutionPipeline;
import com.shiyu.ai.tooling.contract.api.ToolService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 根据输入配置创建 工具 Call Node 相关的流程节点或业务组件。
 */
@Component
public class ToolCallNodeCreator implements NodeCreator {
    /**
     * toolService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ToolService toolService;
    /**
     * executionPipeline 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ToolExecutionPipeline executionPipeline;

    /**
     * 处理toolcallnodecreator。
     *
     * @param toolService toolService 参数。
     *
     * @return 处理结果。
     */
    public ToolCallNodeCreator(ToolService toolService) {
        this(toolService, null);
    }

    /**
     * 构建或转换 工具 Call Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param toolService 用于完成本次业务处理的 toolService 参数。
     * @param executionPipeline 用于完成本次业务处理的 executionPipeline 参数。
     */
    @Autowired
    public ToolCallNodeCreator(ToolService toolService, ToolExecutionPipeline executionPipeline) {
        this.toolService = toolService;
        this.executionPipeline = executionPipeline;
    }

    /**
     * 查询 工具 Call Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 工具 Call Node 相关操作生成的结果数据。
     */
    @Override
    public NodeType getType() {
        return NodeType.TOOL_CALL;
    }

    /**
     * 创建或保存 工具 Call Node 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回 工具 Call Node 相关操作生成的结果数据。
     */
    @Override
    public BaseNode create(NodeConfig config) {
        return ToolCallNode.builder()
                .config((ToolCallConfig) config)
                .toolService(toolService)
                .executionPipeline(executionPipeline)
                .build();
    }
}
