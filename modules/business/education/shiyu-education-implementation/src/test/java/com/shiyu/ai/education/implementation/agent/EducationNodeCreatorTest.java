package com.shiyu.ai.education.implementation.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.education.contract.agent.EducationNodeTypes;
import com.shiyu.ai.education.implementation.agent.node.creator.AbilityQueryNodeCreator;
import com.shiyu.ai.education.implementation.agent.node.creator.PracticeNodeCreator;
import com.shiyu.ai.education.implementation.agent.node.creator.PrereqCheckNodeCreator;
import com.shiyu.ai.education.implementation.agent.node.creator.ReviewScheduleNodeCreator;
import com.shiyu.ai.education.implementation.agent.node.creator.ScoreAnalysisNodeCreator;
import com.shiyu.ai.education.implementation.agent.node.creator.TeachNodeCreator;
import com.shiyu.ai.education.implementation.application.AbilityService;
import com.shiyu.ai.education.implementation.application.ReviewService;
import com.shiyu.ai.education.implementation.domain.ReviewScheduler;
import com.shiyu.ai.education.implementation.domain.port.repository.ReviewTaskRepository;
import com.shiyu.ai.knowledge.contract.api.KnowledgePathPort;
import com.shiyu.ai.knowledge.contract.api.KnowledgePointPort;
import com.shiyu.ai.knowledge.contract.api.KnowledgeRelationPort;
import com.shiyu.ai.model.contract.api.ChatEngine;

import org.junit.jupiter.api.Test;

class EducationNodeCreatorTest {
    private static final NodeConfig CONFIG = NodeConfig.builder().nodeId("education-node").build();

    @Test
    void createsAllEducationNodesWithTheirConfiguration() {
        ChatEngine chat = mock(ChatEngine.class);
        AbilityService ability = mock(AbilityService.class);
        KnowledgePointPort points = mock(KnowledgePointPort.class);
        KnowledgeRelationPort relations = mock(KnowledgeRelationPort.class);
        KnowledgePathPort paths = mock(KnowledgePathPort.class);
        ReviewService review = mock(ReviewService.class);
        ReviewScheduler scheduler = mock(ReviewScheduler.class);
        ReviewTaskRepository tasks = mock(ReviewTaskRepository.class);

        assertCreated(
                new AbilityQueryNodeCreator(points, relations, ability), EducationNodeTypes.ABILITY_QUERY);
        assertCreated(new PracticeNodeCreator(chat), EducationNodeTypes.EDUCATION_PRACTICE);
        assertCreated(new PrereqCheckNodeCreator(relations, paths), EducationNodeTypes.PREREQ_CHECK);
        assertCreated(
                new ReviewScheduleNodeCreator(scheduler, review, tasks), EducationNodeTypes.REVIEW_SCHEDULE);
        assertCreated(new ScoreAnalysisNodeCreator(ability), EducationNodeTypes.SCORE_ANALYSIS);
        assertCreated(new TeachNodeCreator(chat), EducationNodeTypes.EDUCATION_TEACH);
    }

    private static void assertCreated(
            com.shiyu.ai.agent.contract.node.creator.NodeCreator creator, NodeType type) {
        assertEquals(type, creator.getType());
        BaseNode node = creator.create(CONFIG);
        assertSame(CONFIG, node.getConfig());
    }
}
