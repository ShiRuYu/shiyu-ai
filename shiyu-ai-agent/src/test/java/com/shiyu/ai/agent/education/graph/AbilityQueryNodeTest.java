package com.shiyu.ai.agent.education.graph;

import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.education.service.AbilityService;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
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
class AbilityQueryNodeTest {

    @Mock private KnowledgeService knowledgeService;
    @Mock private KnowledgeRelationService knowledgeRelationService;
    @Mock private AbilityService abilityService;

    private AbilityQueryNode node;

    @BeforeEach
    void setUp() {
        node = new AbilityQueryNode(knowledgeService, knowledgeRelationService, abilityService);
    }

    @Test
    void testExecuteSuccess() throws Exception {
        KnowledgeResponse knowledge = new KnowledgeResponse(1L, "MATH_001", "一元二次方程", "代数基础方程", 3, "代数", null, null, null, null);
        when(knowledgeService.getById(1L)).thenReturn(knowledge);
        when(knowledgeRelationService.getPrerequisites(1L)).thenReturn(List.of());
        when(abilityService.get(100L, 1L)).thenReturn(new AbilityValue(100L, 1L, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, java.time.LocalDateTime.now()));

        Map<String, Object> params = new HashMap<>();
        params.put("studentId", 100L);
        params.put("knowledgeId", 1L);
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertTrue(output.isSuccess());
        assertEquals("一元二次方程", output.getData("knowledgeName"));
        assertNotNull(output.getData("knowledge"));
        assertNotNull(output.getData("ability"));
        assertEquals(0.58, (double) output.getData("overallScore"), 0.01);
    }

    @Test
    void testExecuteMissingKnowledgeId() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("studentId", 100L);
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertFalse(output.isSuccess());
        assertTrue(output.getMsg().contains("knowledgeId"));
    }

    @Test
    void testExecuteMissingStudentId() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("knowledgeId", 1L);
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertFalse(output.isSuccess());
        assertTrue(output.getMsg().contains("studentId") || output.getMsg().contains("knowledgeId"));
    }

    @Test
    void testExecuteKnowledgeNotFound() throws Exception {
        when(knowledgeService.getById(999L)).thenReturn(null);

        Map<String, Object> params = new HashMap<>();
        params.put("studentId", 100L);
        params.put("knowledgeId", 999L);
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertFalse(output.isSuccess());
        assertTrue(output.getMsg().contains("不存在"));
    }

    @Test
    void testGetRequiredInputs() {
        var inputs = node.getRequiredInputs();
        assertEquals(2, inputs.size());
    }
}
