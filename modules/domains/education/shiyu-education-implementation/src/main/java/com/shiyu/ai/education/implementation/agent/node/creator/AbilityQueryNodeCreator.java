package com.shiyu.ai.education.implementation.agent.node.creator;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.education.implementation.agent.graph.AbilityQueryNode;
import com.shiyu.ai.education.implementation.application.AbilityService;
import com.shiyu.ai.knowledge.contract.api.KnowledgePointPort;
import com.shiyu.ai.knowledge.contract.api.KnowledgeRelationPort;

import org.springframework.stereotype.Component;

@Component
public class AbilityQueryNodeCreator implements NodeCreator {

    private final KnowledgePointPort knowledgePointService;
    private final KnowledgeRelationPort knowledgeRelationService;
    private final AbilityService abilityService;

    public AbilityQueryNodeCreator(
            KnowledgePointPort knowledgePointService,
            KnowledgeRelationPort knowledgeRelationService,
            AbilityService abilityService) {
        this.knowledgePointService = knowledgePointService;
        this.knowledgeRelationService = knowledgeRelationService;
        this.abilityService = abilityService;
    }

    @Override
    public NodeType getType() {
        return NodeType.ABILITY_QUERY;
    }

    @Override
    public BaseNode create(NodeConfig config) {
        AbilityQueryNode node =
                new AbilityQueryNode(
                        knowledgePointService, knowledgeRelationService, abilityService);
        node.setConfig(config);
        return node;
    }
}
