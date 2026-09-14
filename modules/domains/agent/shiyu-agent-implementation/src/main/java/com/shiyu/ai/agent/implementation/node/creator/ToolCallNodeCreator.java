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
 * {@code ToolCallNodeCreator} 负责创建智能体模块中的运行时对象，并集中封装构造规则。
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
     * {@code ToolCallNodeCreator} 创建并初始化当前类型实例。
     *
     * @param toolService 参数值，用于执行当前操作。
     * @param executionPipeline 参数值，用于执行当前操作。
     */
    @Autowired
    public ToolCallNodeCreator(ToolService toolService, ToolExecutionPipeline executionPipeline) {
        this.toolService = toolService;
        this.executionPipeline = executionPipeline;
    }

    /**
     * {@code getType} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public NodeType getType() {
        return NodeType.TOOL_CALL;
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param config 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
