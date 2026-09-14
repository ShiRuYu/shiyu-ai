package com.shiyu.ai.agent.contract.node.creator;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;

/**
 * NodeCreator 接口，定义智能体模块的能力边界。
 */
public interface NodeCreator {
    /**
     * 获取类型。
     *
     * @return 处理结果。
     */
    NodeType getType();

    /**
     * 创建nodecreator。
     *
     * @param config config 参数。
     *
     * @return 处理结果。
     */
    BaseNode create(NodeConfig config);
}
