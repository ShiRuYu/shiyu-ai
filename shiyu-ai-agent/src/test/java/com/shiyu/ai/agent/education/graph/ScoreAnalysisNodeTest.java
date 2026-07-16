package com.shiyu.ai.agent.education.graph;

import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.education.service.AbilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@Tag("dev")
@ExtendWith(MockitoExtension.class)
class ScoreAnalysisNodeTest {

    @Mock private AbilityService abilityService;

    private ScoreAnalysisNode node;

    @BeforeEach
    void setUp() {
        node = new ScoreAnalysisNode(abilityService);
    }

    @Test
    void testScoreWithAnswerResults() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("studentId", 100L);
        params.put("knowledgeId", 1L);
        params.put("answerResults", List.of(
                Map.of("correct", true),
                Map.of("correct", true),
                Map.of("correct", false),
                Map.of("correct", true),
                Map.of("correct", false)
        ));
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertTrue(output.isSuccess());
        double score = output.getData("practiceScore");
        assertEquals(60.0, score, 0.01);  // 3/5 = 60%
        assertEquals(0.6, (double) output.getData("practiceAccuracy"), 0.01);
        assertFalse((Boolean) output.getData("reviewNeeded"));  // score >= 60
    }

    @Test
    void testScoreWithLowAccuracyTriggersReview() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("studentId", 100L);
        params.put("knowledgeId", 1L);
        params.put("answerResults", List.of(
                Map.of("correct", false),
                Map.of("correct", false),
                Map.of("correct", true)
        ));
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertTrue(output.isSuccess());
        double score = output.getData("practiceScore");
        assertTrue(score < 60.0, "得分低于60应触发 reviewNeeded");
        assertTrue((Boolean) output.getData("reviewNeeded"));
        verify(abilityService).update(anyLong(), anyLong(), any(), anyDouble());
    }

    @Test
    void testScoreWithoutAnswerResultsUsesDefault() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("studentId", 100L);
        params.put("knowledgeId", 1L);
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertTrue(output.isSuccess());
        assertEquals(60.0, (double) output.getData("practiceScore"), 0.01);
        assertEquals(0.6, (double) output.getData("practiceAccuracy"), 0.01);
    }

    @Test
    void testScoreUpdatesAbility() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("studentId", 100L);
        params.put("knowledgeId", 1L);
        params.put("answerResults", List.of(Map.of("correct", true)));
        NodeInput input = NodeInput.fromMap(params);

        node.doExecute(input);

        verify(abilityService).update(100L, 1L, com.shiyu.ai.education.domain.BloomTaxonomy.APPLY, 1.0);
        verify(abilityService).update(100L, 1L, com.shiyu.ai.education.domain.BloomTaxonomy.REMEMBER, 1.0);
    }
}
