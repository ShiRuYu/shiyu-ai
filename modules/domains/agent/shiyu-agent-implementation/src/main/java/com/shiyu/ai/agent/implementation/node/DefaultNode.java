package com.shiyu.ai.agent.implementation.node;

import com.shiyu.ai.agent.contract.node.*;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 执行 Default 相关流程节点的输入处理和状态转移。
 *
 * @author shiyu-ai
 */
@Setter
@Getter
@Slf4j
public class DefaultNode extends BaseNode {

    /**
     * 私有构造函数，强制使用 Builder 模式
     *
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
     *
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 构建 Builder 相关的对象、流程或运行时配置。
     */
    public static class Builder {
        /**
         * 配置，表示当前对象中的对应属性。
         */
        private NodeConfig config;

        /**
         * 设置节点配置
         *
         * @param config 节点配置
         * @return Builder 实例
         */
        public Builder config(NodeConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 构建并返回 DefaultNode 实例
         *
         * @return DefaultNode 实例
         */
        public DefaultNode build() {
            return new DefaultNode(config);
        }
    }

    /**
     * 执行 Default 相关业务数据，并返回处理结果。
     *
     * @param input 用于完成本次业务处理的 input 参数。
     * @return 返回 Default 相关操作生成的结果数据。
     */
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

            log.info(
                    "默认节点执行完成，输入参数数量：{}",
                    input == null || input.toMap() == null ? 0 : input.toMap().size());
            return output;

        } catch (Exception e) {
            log.error("默认节点执行失败", e);
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("默认节点执行失败，请稍后重试");
            return output;
        }
    }

    /**
     * 查询 Default 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of();
    }
}
