package com.shiyu.ai.agent.node.llm;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * LLM 调用节点
 * 用于调用大语言模型生成回复
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
@Component
public class LlmCallNode extends BaseNode {

    private LlmCallConfig config;

    public LlmCallNode() {
        this.config = new LlmCallConfig();
        // 设置节点类型为 LLM_CALL
        this.config.setNodeType(NodeType.LLM_CALL);
    }

    public LlmCallNode(LlmCallConfig config) {
        super(config);
        this.config = config;
        // 设置节点类型为 LLM_CALL
        this.config.setNodeType(NodeType.LLM_CALL);
    }

    public void setLlmCallConfig(LlmCallConfig config) {
        super.setConfig(config);
        this.config = config;
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行 LLM 调用节点：{}", config.getNodeName());
        log.debug("LLM 配置：modelName={}, temperature={}, maxTokens={}", 
                config.getModelName(), config.getTemperature(), config.getMaxTokens());
        
        try {
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("LLM 调用节点执行成功");
            
            // TODO: 实现具体的 LLM 调用逻辑
            
            log.info("LLM 调用节点执行完成，输入：{}", input);
            return output;
            
        } catch (Exception e) {
            log.error("LLM 调用节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("LLM 调用节点执行失败：" + e.getMessage());
            return output;
        }
    }
}
