package com.shiyu.ai.agent.node.memory;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 短期记忆节点
 * 用于存储和管理最近的对话历史
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
@Component
public class ShortTermMemoryNode extends BaseNode {

    private ShortTermMemoryConfig config;

    public ShortTermMemoryNode() {
        this.config = new ShortTermMemoryConfig();
        // 设置节点类型为 MEMORY_SHORT_TERM
        this.config.setNodeType(NodeType.MEMORY_SHORT_TERM);
    }

    public ShortTermMemoryNode(ShortTermMemoryConfig config) {
        super(config);
        this.config = config;
        // 设置节点类型为 MEMORY_SHORT_TERM
        this.config.setNodeType(NodeType.MEMORY_SHORT_TERM);
    }

    public void setShortTermMemoryConfig(ShortTermMemoryConfig config) {
        super.setConfig(config);
        this.config = config;
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行短期记忆节点：{}", config.getNodeName());
        log.debug("记忆配置：maxMessages={}, enableSlidingWindow={}", 
                config.getMaxMessages(), config.getEnableSlidingWindow());
        
        try {
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("短期记忆节点执行成功");
            
            // TODO: 实现具体的短期记忆管理逻辑
            
            log.info("短期记忆节点执行完成，输入：{}", input);
            return output;
            
        } catch (Exception e) {
            log.error("短期记忆节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("短期记忆节点执行失败：" + e.getMessage());
            return output;
        }
    }
}
