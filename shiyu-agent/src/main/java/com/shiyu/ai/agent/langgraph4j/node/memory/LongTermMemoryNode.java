package com.shiyu.ai.agent.langgraph4j.node.memory;

import com.shiyu.ai.agent.langgraph4j.node.BaseNode;
import com.shiyu.ai.agent.langgraph4j.node.NodeInput;
import com.shiyu.ai.agent.langgraph4j.node.NodeOutput;
import com.shiyu.ai.agent.langgraph4j.node.NodeType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
public class LongTermMemoryNode extends BaseNode {

    private LongTermMemoryConfig config;

    /**
     * 私有构造函数，强制使用 Builder 模式
     * @param config 节点配置
     */
    private LongTermMemoryNode(LongTermMemoryConfig config) {
        super(config != null ? config : new LongTermMemoryConfig());
        this.config = config != null ? config : new LongTermMemoryConfig();
        // 设置节点类型为 MEMORY_LONG_TERM
        this.config.setNodeType(NodeType.MEMORY_LONG_TERM);
    }

    /**
     * 获取 Builder 实例
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 LongTermMemoryNode 实例
     */
    public static class Builder {
        private LongTermMemoryConfig config;

        /**
         * 设置节点配置
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(LongTermMemoryConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 构建并返回 LongTermMemoryNode 实例
         * @return LongTermMemoryNode 实例
         */
        public LongTermMemoryNode build() {
            return new LongTermMemoryNode(config);
        }
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
