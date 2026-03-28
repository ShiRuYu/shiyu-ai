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
 * 长期记忆节点
 * 用于存储和管理重要信息和知识点
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
@Component
public class LongTermMemoryNode extends BaseNode {

    private LongTermMemoryConfig config;

    public LongTermMemoryNode() {
        this.config = new LongTermMemoryConfig();
        // 设置节点类型为 MEMORY_LONG_TERM
        this.config.setNodeType(NodeType.MEMORY_LONG_TERM);
    }

    public LongTermMemoryNode(LongTermMemoryConfig config) {
        super(config);
        this.config = config;
        // 设置节点类型为 MEMORY_LONG_TERM
        this.config.setNodeType(NodeType.MEMORY_LONG_TERM);
    }

    public void setLongTermMemoryConfig(LongTermMemoryConfig config) {
        super.setConfig(config);
        this.config = config;
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行长期记忆节点：{}", config.getNodeName());
        log.debug("记忆配置：storageType={}, embeddingModel={}, minImportanceScore={}", 
                config.getStorageType(), config.getEmbeddingModel(), config.getMinImportanceScore());
        
        try {
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("长期记忆节点执行成功");
            
            // TODO: 实现具体的长期记忆管理逻辑
            
            log.info("长期记忆节点执行完成，输入：{}", input);
            return output;
            
        } catch (Exception e) {
            log.error("长期记忆节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("长期记忆节点执行失败：" + e.getMessage());
            return output;
        }
    }
}
