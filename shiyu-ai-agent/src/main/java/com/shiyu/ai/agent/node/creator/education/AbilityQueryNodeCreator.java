package com.shiyu.ai.agent.node.creator.education;

import com.shiyu.ai.agent.education.graph.AbilityQueryNode;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.creator.NodeCreator;
import com.shiyu.ai.education.service.AbilityService;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import org.springframework.stereotype.Component;

@Component
public class AbilityQueryNodeCreator implements NodeCreator {

    private final KnowledgeService knowledgeService;
    private final KnowledgeRelationService knowledgeRelationService;
    private final AbilityService abilityService;

    public AbilityQueryNodeCreator(KnowledgeService knowledgeService,
                                   KnowledgeRelationService knowledgeRelationService,
                                   AbilityService abilityService) {
        this.knowledgeService = knowledgeService;
        this.knowledgeRelationService = knowledgeRelationService;
        this.abilityService = abilityService;
    }

    @Override
    public NodeType getType() {
        return NodeType.ABILITY_QUERY;
    }

    @Override
    public BaseNode create(NodeConfig config) {
        AbilityQueryNode node = new AbilityQueryNode(knowledgeService, knowledgeRelationService, abilityService);
        node.setConfig(config);
        return node;
    }
}
