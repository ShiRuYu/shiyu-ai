package com.shiyu.ai.agent.node;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 默认节点实现
 * 用于通用处理逻辑，当没有特定节点类型时使用
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
@Component
public class DefaultNode extends BaseNode {

    public DefaultNode() {
        this.config = new NodeConfig();
        // 设置节点类型为 DEFAULT
        this.config.setNodeType(NodeType.DEFAULT);
    }

    public DefaultNode(NodeConfig config) {
        super(config);
        // 设置节点类型为 DEFAULT
        this.config.setNodeType(NodeType.DEFAULT);
    }

    @Override
    public NodeOutput doExecute(NodeInput input) {
        log.info("执行默认节点：{}", config.getNodeName());
        
        try {
            // 默认节点执行通用逻辑
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("默认节点执行成功");
            
            // 可以在这里添加通用的数据处理逻辑
            // 例如：简单的数据转换、日志记录等
            
            log.info("默认节点执行完成，输入：{}", input);
            return output;
            
        } catch (Exception e) {
            log.error("默认节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("默认节点执行失败：" + e.getMessage());
            return output;
        }
    }
}
