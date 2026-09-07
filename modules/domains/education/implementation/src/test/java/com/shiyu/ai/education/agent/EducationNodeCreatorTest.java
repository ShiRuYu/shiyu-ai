package com.shiyu.ai.education.agent;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.education.agent.node.creator.AbilityQueryNodeCreator;
import com.shiyu.ai.education.agent.node.creator.PracticeNodeCreator;
import com.shiyu.ai.education.agent.node.creator.PrereqCheckNodeCreator;
import com.shiyu.ai.education.agent.node.creator.ReviewScheduleNodeCreator;
import com.shiyu.ai.education.agent.node.creator.ScoreAnalysisNodeCreator;
import com.shiyu.ai.education.agent.node.creator.TeachNodeCreator;
import com.shiyu.ai.education.domain.ReviewScheduler;
import com.shiyu.ai.education.service.AbilityService;
import com.shiyu.ai.education.service.ReviewService;
import com.shiyu.ai.education.port.repository.ReviewTaskRepository;
import com.shiyu.ai.knowledge.port.KnowledgePathPort;
import com.shiyu.ai.knowledge.port.KnowledgePointPort;
import com.shiyu.ai.knowledge.port.KnowledgeRelationPort;
import com.shiyu.ai.model.chat.ChatEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

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

        assertCreated(new AbilityQueryNodeCreator(points, relations, ability), NodeType.ABILITY_QUERY);
        assertCreated(new PracticeNodeCreator(chat), NodeType.EDUCATION_PRACTICE);
        assertCreated(new PrereqCheckNodeCreator(relations, paths), NodeType.PREREQ_CHECK);
        assertCreated(new ReviewScheduleNodeCreator(scheduler, review, tasks), NodeType.REVIEW_SCHEDULE);
        assertCreated(new ScoreAnalysisNodeCreator(ability), NodeType.SCORE_ANALYSIS);
        assertCreated(new TeachNodeCreator(chat), NodeType.EDUCATION_TEACH);
    }

    private static void assertCreated(com.shiyu.ai.agent.node.creator.NodeCreator creator, NodeType type) {
        assertEquals(type, creator.getType());
        BaseNode node = creator.create(CONFIG);
        assertSame(CONFIG, node.getConfig());
    }
}
