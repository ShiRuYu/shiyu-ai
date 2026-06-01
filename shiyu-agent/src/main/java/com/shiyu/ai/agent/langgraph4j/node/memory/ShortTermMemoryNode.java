package com.shiyu.ai.agent.langgraph4j.node.memory;

import com.shiyu.ai.agent.langgraph4j.node.BaseNode;
import com.shiyu.ai.agent.langgraph4j.node.NodeInput;
import com.shiyu.ai.agent.langgraph4j.node.NodeOutput;
import com.shiyu.ai.agent.langgraph4j.node.NodeType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
public class ShortTermMemoryNode extends BaseNode {

    private ShortTermMemoryConfig config;

    /**
     * 私有构造函数，强制使用 Builder 模式
     * @param config 节点配置
     */
    private ShortTermMemoryNode(ShortTermMemoryConfig config) {
        super(config != null ? config : new ShortTermMemoryConfig());
        this.config = config != null ? config : new ShortTermMemoryConfig();
        // 设置节点类型为 MEMORY_SHORT_TERM
        this.config.setNodeType(NodeType.MEMORY_SHORT_TERM);
    }

    /**
     * 获取 Builder 实例
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 ShortTermMemoryNode 实例
     */
    public static class Builder {
        private ShortTermMemoryConfig config;

        /**
         * 设置节点配置
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(ShortTermMemoryConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 构建并返回 ShortTermMemoryNode 实例
         * @return ShortTermMemoryNode 实例
         */
        public ShortTermMemoryNode build() {
            return new ShortTermMemoryNode(config);
        }
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
            
            log.warn("短期记忆节点尚未实现完整逻辑，请集成缓存/数据库存储");
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
