package com.shiyu.ai.education.implementation.agent.node.creator;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.education.contract.agent.EducationNodeTypes;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.education.implementation.agent.graph.AbilityQueryNode;
import com.shiyu.ai.education.implementation.application.AbilityService;
import com.shiyu.ai.knowledge.contract.api.KnowledgePointPort;
import com.shiyu.ai.knowledge.contract.api.KnowledgeRelationPort;

import org.springframework.stereotype.Component;

/**
 * {@code AbilityQueryNodeCreator} 负责创建教育模块中的运行时对象，并集中封装构造规则。
 */
@Component
public class AbilityQueryNodeCreator implements NodeCreator {

    /**
     * knowledgePointService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgePointPort knowledgePointService;
    /**
     * knowledgeRelationService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeRelationPort knowledgeRelationService;
    /**
     * abilityService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AbilityService abilityService;

    /**
     * {@code AbilityQueryNodeCreator} 创建并初始化当前类型实例。
     *
     * @param knowledgePointService 参数值，用于执行当前操作。
     * @param knowledgeRelationService 参数值，用于执行当前操作。
     * @param abilityService 参数值，用于执行当前操作。
     */
    public AbilityQueryNodeCreator(
            KnowledgePointPort knowledgePointService,
            KnowledgeRelationPort knowledgeRelationService,
            AbilityService abilityService) {
        this.knowledgePointService = knowledgePointService;
        this.knowledgeRelationService = knowledgeRelationService;
        this.abilityService = abilityService;
    }

    /**
     * {@code getType} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public NodeType getType() {
        return EducationNodeTypes.ABILITY_QUERY;
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
        AbilityQueryNode node =
                new AbilityQueryNode(
                        knowledgePointService, knowledgeRelationService, abilityService);
        node.setConfig(config);
        return node;
    }
}
