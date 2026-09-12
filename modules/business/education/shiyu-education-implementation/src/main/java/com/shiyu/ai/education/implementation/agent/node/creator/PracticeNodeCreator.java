package com.shiyu.ai.education.implementation.agent.node.creator;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.education.contract.agent.EducationNodeTypes;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.education.implementation.agent.graph.PracticeNode;
import com.shiyu.ai.model.contract.api.ChatEngine;

import org.springframework.stereotype.Component;

/**
 * {@code PracticeNodeCreator} 负责创建教育模块中的运行时对象，并集中封装构造规则。
 */
@Component
public class PracticeNodeCreator implements NodeCreator {

    /**
     * chatEngine 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ChatEngine chatEngine;

    /**
     * {@code PracticeNodeCreator} 创建并初始化当前类型实例。
     *
     * @param chatEngine 参数值，用于执行当前操作。
     */
    public PracticeNodeCreator(ChatEngine chatEngine) {
        this.chatEngine = chatEngine;
    }

    /**
     * {@code getType} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public NodeType getType() {
        return EducationNodeTypes.EDUCATION_PRACTICE;
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param config 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public BaseNode create(NodeConfig config) {
        PracticeNode node = new PracticeNode(chatEngine);
        node.setConfig(config);
        return node;
    }
}
