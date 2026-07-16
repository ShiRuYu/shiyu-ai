package com.shiyu.ai.agent.node.creator.education;

import com.shiyu.ai.agent.education.graph.PrereqCheckNode;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.node.creator.NodeCreator;
import com.shiyu.ai.knowledge.path.LearningPathService;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import org.springframework.stereotype.Component;

@Component
public class PrereqCheckNodeCreator implements NodeCreator {

    private final KnowledgeService knowledgeService;
    private final KnowledgeRelationService knowledgeRelationService;
    private final LearningPathService learningPathService;

    public PrereqCheckNodeCreator(KnowledgeService knowledgeService,
                                  KnowledgeRelationService knowledgeRelationService,
                                  LearningPathService learningPathService) {
        this.knowledgeService = knowledgeService;
        this.knowledgeRelationService = knowledgeRelationService;
        this.learningPathService = learningPathService;
    }

    @Override
    public NodeType getType() {
        return NodeType.PREREQ_CHECK;
    }

    @Override
    public BaseNode create(NodeConfig config) {
        PrereqCheckNode node = new PrereqCheckNode(knowledgeService, knowledgeRelationService, learningPathService);
        node.setConfig(config);
        return node;
    }
}
