package com.shiyu.ai.agent.node.transform;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 数据转换节点
 * 用于数据格式转换或处理
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
@Component
public class TransformNode extends BaseNode {

    private TransformConfig config;

    public TransformNode() {
        this.config = new TransformConfig();
        // 设置节点类型为 TRANSFORM
        this.config.setNodeType(NodeType.TRANSFORM);
    }

    public TransformNode(TransformConfig config) {
        super(config);
        this.config = config;
        // 设置节点类型为 TRANSFORM
        this.config.setNodeType(NodeType.TRANSFORM);
    }

    public void setTransformConfig(TransformConfig config) {
        super.setConfig(config);
        this.config = config;
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行数据转换节点：{}", config.getNodeName());
        log.debug("转换配置：transformType={}, inputFormat={}, outputFormat={}", 
                config.getTransformType(), config.getInputFormat(), config.getOutputFormat());
        
        try {
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("数据转换节点执行成功");
            
            // TODO: 实现具体的数据转换逻辑
            
            log.info("数据转换节点执行完成，输入：{}", input);
            return output;
            
        } catch (Exception e) {
            log.error("数据转换节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("数据转换节点执行失败：" + e.getMessage());
            return output;
        }
    }
}
