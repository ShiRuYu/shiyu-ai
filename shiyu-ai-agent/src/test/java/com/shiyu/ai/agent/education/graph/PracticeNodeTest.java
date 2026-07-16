package com.shiyu.ai.agent.education.graph;

import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("dev")
@ExtendWith(MockitoExtension.class)
class PracticeNodeTest {

    @Mock private ChatEngine chatEngine;

    private PracticeNode node;

    @BeforeEach
    void setUp() {
        node = new PracticeNode(chatEngine);
    }

    @Test
    void testPracticeSuccess() throws Exception {
        KnowledgeResponse knowledge = new KnowledgeResponse(1L, "MATH_001", "一元二次方程", "", 3, "代数", null, null, null, null);
        String llmResponse = "{\"type\":\"CHOICE\",\"title\":\"x²-5x+6=0的解是\",\"options\":[\"A.2,3\",\"B.-2,-3\",\"C.2,-3\",\"D.-2,3\"],\"answer\":\"A\",\"analysis\":\"因式分解\",\"ability_dimension\":\"apply\"}";
        when(chatEngine.chat(any(ChatRequest.class)))
                .thenReturn(ChatResponse.builder().success(true).content(llmResponse).build());

        Map<String, Object> params = new HashMap<>();
        params.put("knowledge", knowledge);
        params.put("overallScore", 0.6);
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertTrue(output.isSuccess());
        assertTrue((Boolean) output.getData("practiceDone"));
        assertTrue(((Integer) output.getData("questionCount")) > 0);
    }

    @Test
    void testPracticeMissingKnowledge() throws Exception {
        Map<String, Object> params = new HashMap<>();
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertFalse(output.isSuccess());
        assertTrue(output.getMsg().contains("knowledge"));
    }

    @Test
    void testPracticeLlmFailure() throws Exception {
        KnowledgeResponse knowledge = new KnowledgeResponse(1L, "MATH_001", "一元二次方程", "", 3, "代数", null, null, null, null);
        when(chatEngine.chat(any(ChatRequest.class)))
                .thenReturn(ChatResponse.builder().success(false).errorMessage("LLM 不可用").build());

        Map<String, Object> params = new HashMap<>();
        params.put("knowledge", knowledge);
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertFalse(output.isSuccess());
        assertEquals(0, ((Number) output.getData("questionCount")).intValue());
    }
}
