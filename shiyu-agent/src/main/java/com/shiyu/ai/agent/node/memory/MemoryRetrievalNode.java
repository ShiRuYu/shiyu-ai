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
 * 记忆检索节点
 * 用于从记忆中检索相关信息
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
@Component
public class MemoryRetrievalNode extends BaseNode {

    private MemoryRetrievalConfig config;

    public MemoryRetrievalNode() {
        this.config = new MemoryRetrievalConfig();
        // 设置节点类型为 MEMORY_RETRIEVAL
        this.config.setNodeType(NodeType.MEMORY_RETRIEVAL);
    }

    public MemoryRetrievalNode(MemoryRetrievalConfig config) {
        super(config);
        this.config = config;
        // 设置节点类型为 MEMORY_RETRIEVAL
        this.config.setNodeType(NodeType.MEMORY_RETRIEVAL);
    }

    public void setMemoryRetrievalConfig(MemoryRetrievalConfig config) {
        super.setConfig(config);
        this.config = config;
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行记忆检索节点：{}", config.getNodeName());
        log.debug("检索配置：retrievalScope={}, topK={}, similarityThreshold={}", 
                config.getRetrievalScope(), config.getTopK(), config.getSimilarityThreshold());
        
        try {
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("记忆检索节点执行成功");
            
            // TODO: 实现具体的记忆检索逻辑
            
            log.info("记忆检索节点执行完成，输入：{}", input);
            return output;
            
        } catch (Exception e) {
            log.error("记忆检索节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("记忆检索节点执行失败：" + e.getMessage());
            return output;
        }
    }
}
