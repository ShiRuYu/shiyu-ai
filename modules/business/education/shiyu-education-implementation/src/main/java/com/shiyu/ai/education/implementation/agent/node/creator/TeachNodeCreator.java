package com.shiyu.ai.education.implementation.agent.node.creator;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.education.contract.agent.EducationNodeTypes;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.education.implementation.agent.graph.TeachNode;
import com.shiyu.ai.model.contract.api.ChatEngine;

import org.springframework.stereotype.Component;

/**
 * 根据输入配置创建 Teach Node 相关的流程节点或业务组件。
 */
@Component
public class TeachNodeCreator implements NodeCreator {

    /**
     * chatEngine 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ChatEngine chatEngine;

    /**
     * 执行 Teach Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param chatEngine 用于完成本次业务处理的 chatEngine 参数。
     */
    public TeachNodeCreator(ChatEngine chatEngine) {
        this.chatEngine = chatEngine;
    }

    /**
     * 查询 Teach Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Teach Node 相关操作生成的结果数据。
     */
    @Override
    public NodeType getType() {
        return EducationNodeTypes.EDUCATION_TEACH;
    }

    /**
     * 创建或保存 Teach Node 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回 Teach Node 相关操作生成的结果数据。
     */
    @Override
    public BaseNode create(NodeConfig config) {
        TeachNode node = new TeachNode(chatEngine);
        node.setConfig(config);
        return node;
    }
}
