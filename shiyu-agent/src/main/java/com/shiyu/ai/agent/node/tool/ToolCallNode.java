package com.shiyu.ai.agent.node.tool;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 工具调用节点
 * 用于调用外部工具或服务
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
@Component
public class ToolCallNode extends BaseNode {

    private ToolCallConfig config;

    public ToolCallNode() {
        this.config = new ToolCallConfig();
        // 设置节点类型为 TOOL_CALL
        this.config.setNodeType(NodeType.TOOL_CALL);
    }

    public ToolCallNode(ToolCallConfig config) {
        super(config);
        this.config = config;
        // 设置节点类型为 TOOL_CALL
        this.config.setNodeType(NodeType.TOOL_CALL);
    }

    public void setToolCallConfig(ToolCallConfig config) {
        super.setConfig(config);
        this.config = config;
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行工具调用节点：{}", config.getNodeName());
        log.debug("工具配置：toolName={}, toolType={}, timeout={}", 
                config.getToolName(), config.getToolType(), config.getToolTimeout());
        
        try {
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("工具调用节点执行成功");
            
            // TODO: 实现具体的工具调用逻辑
            
            log.info("工具调用节点执行完成，输入：{}", input);
            return output;
            
        } catch (Exception e) {
            log.error("工具调用节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("工具调用节点执行失败：" + e.getMessage());
            return output;
        }
    }
}
