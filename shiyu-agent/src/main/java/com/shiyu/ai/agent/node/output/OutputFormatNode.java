package com.shiyu.ai.agent.node.output;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 输出格式化节点
 * 用于格式化最终输出结果
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
@Component
public class OutputFormatNode extends BaseNode {

    private OutputFormatConfig config;

    public OutputFormatNode() {
        this.config = new OutputFormatConfig();
        // 设置节点类型为 OUTPUT_FORMAT
        this.config.setNodeType(NodeType.OUTPUT_FORMAT);
    }

    public OutputFormatNode(OutputFormatConfig config) {
        super(config);
        this.config = config;
        // 设置节点类型为 OUTPUT_FORMAT
        this.config.setNodeType(NodeType.OUTPUT_FORMAT);
    }

    public void setOutputFormatConfig(OutputFormatConfig config) {
        super.setConfig(config);
        this.config = config;
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行输出格式化节点：{}", config.getNodeName());
        log.debug("格式化配置：outputFormat={}, prettyPrint={}, template={}", 
                config.getOutputFormat(), config.getPrettyPrint(), config.getTemplate());
        
        try {
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("输出格式化节点执行成功");
            
            // TODO: 实现具体的输出格式化逻辑
            
            log.info("输出格式化节点执行完成，输入：{}", input);
            return output;
            
        } catch (Exception e) {
            log.error("输出格式化节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("输出格式化节点执行失败：" + e.getMessage());
            return output;
        }
    }
}
