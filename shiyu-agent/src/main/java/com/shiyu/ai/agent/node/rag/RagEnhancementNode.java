package com.shiyu.ai.agent.node.rag;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG 增强节点
 * 用于对检索结果进行增强处理
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Setter
@Getter
@Slf4j
public class RagEnhancementNode extends BaseNode {

    private RagEnhancementConfig config;

    /**
     * 私有构造函数，强制使用 Builder 模式
     * @param config 节点配置
     */
    private RagEnhancementNode(RagEnhancementConfig config) {
        super(config != null ? config : new RagEnhancementConfig());
        this.config = config != null ? config : new RagEnhancementConfig();
        // 设置节点类型为 RAG_ENHANCEMENT
        this.config.setNodeType(NodeType.RAG_ENHANCEMENT);
    }

    /**
     * 获取 Builder 实例
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 RagEnhancementNode 实例
     */
    public static class Builder {
        private RagEnhancementConfig config;

        /**
         * 设置节点配置
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(RagEnhancementConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 构建并返回 RagEnhancementNode 实例
         * @return RagEnhancementNode 实例
         */
        public RagEnhancementNode build() {
            return new RagEnhancementNode(config);
        }
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("执行 RAG 增强节点：{}", config.getNodeName());
        log.debug("增强配置：strategy={}, addContext={}, contextWindowSize={}", 
                config.getEnhancementStrategy(), config.getAddContext(), config.getContextWindowSize());
        
        try {
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("RAG 增强节点执行成功");
            
            // TODO: 实现具体的 RAG 增强逻辑
            
            log.info("RAG 增强节点执行完成，输入：{}", input);
            return output;
            
        } catch (Exception e) {
            log.error("RAG 增强节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("RAG 增强节点执行失败：" + e.getMessage());
            return output;
        }
    }
}
