package com.shiyu.ai.agent.education.graph;

import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.path.LearningPathService;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@Tag("dev")
@ExtendWith(MockitoExtension.class)
class PrereqCheckNodeTest {

    @Mock private KnowledgeService knowledgeService;
    @Mock private KnowledgeRelationService knowledgeRelationService;
    @Mock private LearningPathService learningPathService;

    private PrereqCheckNode node;

    @BeforeEach
    void setUp() {
        node = new PrereqCheckNode(knowledgeService, knowledgeRelationService, learningPathService);
    }

    @Test
    void testPrereqCheckNoMissing() throws Exception {
        KnowledgeResponse prereq = new KnowledgeResponse(2L, "MATH_PRE", "基础运算", "加减乘除", 1, "代数", null, null, null, null);
        when(knowledgeRelationService.getPrerequisites(1L)).thenReturn(List.of(prereq));
        when(learningPathService.findMissingPrerequisites(1L, java.util.Set.of())).thenReturn(List.of());

        Map<String, Object> params = new HashMap<>();
        params.put("knowledgeId", 1L);
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertTrue(output.isSuccess());
        assertEquals(1, ((List<?>) output.getData("prerequisites")).size());
        assertFalse((Boolean) output.getData("hasMissingPrereqs"));
    }

    @Test
    void testPrereqCheckHasMissing() throws Exception {
        when(knowledgeRelationService.getPrerequisites(1L)).thenReturn(List.of());
        when(learningPathService.findMissingPrerequisites(1L, java.util.Set.of())).thenReturn(List.of(2L, 3L));

        Map<String, Object> params = new HashMap<>();
        params.put("knowledgeId", 1L);
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertTrue(output.isSuccess());
        assertTrue((Boolean) output.getData("hasMissingPrereqs"));
        List<Long> missingIds = output.getData("missingPrerequisiteIds");
        assertEquals(2, missingIds.size());
    }

    @Test
    void testPrereqCheckMissingKnowledgeId() throws Exception {
        Map<String, Object> params = new HashMap<>();
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertFalse(output.isSuccess());
    }
}
