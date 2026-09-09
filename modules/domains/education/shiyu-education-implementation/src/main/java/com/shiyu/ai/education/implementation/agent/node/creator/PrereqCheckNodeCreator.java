package com.shiyu.ai.education.implementation.agent.node.creator;

import com.shiyu.ai.education.implementation.agent.graph.PrereqCheckNode;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.knowledge.contract.api.KnowledgePathPort;
import com.shiyu.ai.knowledge.contract.api.KnowledgeRelationPort;
import org.springframework.stereotype.Component;

@Component
public class PrereqCheckNodeCreator implements NodeCreator {

    private final KnowledgeRelationPort knowledgeRelationService;
    private final KnowledgePathPort knowledgePathService;

    public PrereqCheckNodeCreator(KnowledgeRelationPort knowledgeRelationService,
                                  KnowledgePathPort knowledgePathService) {
        this.knowledgeRelationService = knowledgeRelationService;
        this.knowledgePathService = knowledgePathService;
    }

    @Override
    public NodeType getType() {
        return NodeType.PREREQ_CHECK;
    }

    @Override
    public BaseNode create(NodeConfig config) {
        PrereqCheckNode node = new PrereqCheckNode(knowledgeRelationService, knowledgePathService);
        node.setConfig(config);
        return node;
    }
}


