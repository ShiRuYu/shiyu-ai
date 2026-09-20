package com.shiyu.ai.education.implementation.agent.node.creator;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.education.contract.agent.EducationNodeTypes;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.education.implementation.agent.graph.PrereqCheckNode;
import com.shiyu.ai.knowledge.contract.api.KnowledgePathPort;
import com.shiyu.ai.knowledge.contract.api.KnowledgeRelationPort;

import org.springframework.stereotype.Component;

/**
 * 根据输入配置创建 Prereq Check Node 相关的流程节点或业务组件。
 */
@Component
public class PrereqCheckNodeCreator implements NodeCreator {

    /**
     * knowledgeRelationService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeRelationPort knowledgeRelationService;
    /**
     * knowledgePathService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgePathPort knowledgePathService;

    /**
     * 执行 Prereq Check Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param knowledgeRelationService 用于完成本次业务处理的 knowledgeRelationService 参数。
     * @param knowledgePathService 用于完成本次业务处理的 knowledgePathService 参数。
     */
    public PrereqCheckNodeCreator(
            KnowledgeRelationPort knowledgeRelationService,
            KnowledgePathPort knowledgePathService) {
        this.knowledgeRelationService = knowledgeRelationService;
        this.knowledgePathService = knowledgePathService;
    }

    /**
     * 查询 Prereq Check Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Prereq Check Node 相关操作生成的结果数据。
     */
    @Override
    public NodeType getType() {
        return EducationNodeTypes.PREREQ_CHECK;
    }

    /**
     * 创建或保存 Prereq Check Node 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回 Prereq Check Node 相关操作生成的结果数据。
     */
    @Override
    public BaseNode create(NodeConfig config) {
        PrereqCheckNode node = new PrereqCheckNode(knowledgeRelationService, knowledgePathService);
        node.setConfig(config);
        return node;
    }
}
