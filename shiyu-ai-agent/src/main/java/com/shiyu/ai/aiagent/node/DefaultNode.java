package com.shiyu.ai.aiagent.node;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
public class DefaultNode extends BaseNode {

    /**
     * 私有构造函数，强制使用 Builder 模式
     * @param config 节点配置
     */
    private DefaultNode(NodeConfig config) {
        super(config != null ? config : new NodeConfig());
        this.config = config != null ? config : new NodeConfig();
        // 设置节点类型为 DEFAULT
        this.config.setNodeType(NodeType.DEFAULT);
    }

    /**
     * 获取 Builder 实例
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类，用于构建 DefaultNode 实例
     */
    public static class Builder {
        private NodeConfig config;

        /**
         * 设置节点配置
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(NodeConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 构建并返回 DefaultNode 实例
         * @return DefaultNode 实例
         */
        public DefaultNode build() {
            return new DefaultNode(config);
        }
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
