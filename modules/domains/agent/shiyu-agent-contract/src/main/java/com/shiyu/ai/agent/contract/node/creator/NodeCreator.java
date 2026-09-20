package com.shiyu.ai.agent.contract.node.creator;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;

/**
 * 根据输入配置创建 Node 相关的流程节点或业务组件。
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
