package com.shiyu.ai.agent.node.creator.education;

import com.shiyu.ai.agent.education.graph.PrereqCheckNode;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.creator.NodeCreator;
import com.shiyu.ai.knowledge.path.KnowledgePathService;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import org.springframework.stereotype.Component;

@Component
public class PrereqCheckNodeCreator implements NodeCreator {

    private final KnowledgeRelationService knowledgeRelationService;
    private final KnowledgePathService knowledgePathService;

    public PrereqCheckNodeCreator(KnowledgeRelationService knowledgeRelationService,
                                  KnowledgePathService knowledgePathService) {
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
