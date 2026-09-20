package com.shiyu.ai.agent.implementation.node.creator;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.agent.implementation.node.intent.IntentConfig;
import com.shiyu.ai.agent.implementation.node.intent.IntentNode;
import com.shiyu.ai.agent.implementation.service.IntentService;

import org.springframework.stereotype.Component;

/**
 * 根据输入配置创建 Intent Node 相关的流程节点或业务组件。
 */
@Component
public class IntentNodeCreator implements NodeCreator {
    /**
     * intentService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final IntentService intentService;

    /**
     * 执行 Intent Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param intentService 用于完成本次业务处理的 intentService 参数。
     */
    public IntentNodeCreator(IntentService intentService) {
        this.intentService = intentService;
    }

    /**
     * 查询 Intent Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Intent Node 相关操作生成的结果数据。
     */
    @Override
    public NodeType getType() {
        return NodeType.INTENT;
    }

    /**
     * 创建或保存 Intent Node 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回 Intent Node 相关操作生成的结果数据。
     */
    @Override
    public BaseNode create(NodeConfig config) {
        return IntentNode.builder()
                .config((IntentConfig) config)
                .intentService(intentService)
                .build();
    }
}
