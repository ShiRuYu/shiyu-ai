package com.shiyu.ai.agent.node.condition;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 条件判断节点
 * 用于根据条件决定执行路径
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
@Component
public class ConditionNode extends BaseNode {

    private ConditionConfig config;

    public ConditionNode() {
        this.config = new ConditionConfig();
        // 设置节点类型为 CONDITION
        this.config.setNodeType(NodeType.CONDITION);
    }

    public ConditionNode(ConditionConfig config) {
        super(config);
        this.config = config;
        // 设置节点类型为 CONDITION
        this.config.setNodeType(NodeType.CONDITION);
    }

    public void setConditionConfig(ConditionConfig config) {
        super.setConfig(config);
        this.config = config;
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行条件判断节点：{}", config.getNodeName());
        log.debug("条件配置：conditionType={}, conditionExpression={}, defaultBranch={}", 
                config.getConditionType(), config.getConditionExpression(), config.getDefaultBranch());
        
        try {
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("条件判断节点执行成功");
            
            // TODO: 实现具体的条件判断逻辑
            
            log.info("条件判断节点执行完成，输入：{}", input);
            return output;
            
        } catch (Exception e) {
            log.error("条件判断节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("条件判断节点执行失败：" + e.getMessage());
            return output;
        }
    }
}
