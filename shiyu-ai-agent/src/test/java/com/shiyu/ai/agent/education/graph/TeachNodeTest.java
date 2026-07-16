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
class TeachNodeTest {

    @Mock private ChatEngine chatEngine;

    private TeachNode node;

    @BeforeEach
    void setUp() {
        node = new TeachNode(chatEngine);
    }

    @Test
    void testTeachSuccess() throws Exception {
        KnowledgeResponse knowledge = new KnowledgeResponse(1L, "PHYS_001", "牛顿定律", "经典力学基础", 3, "物理", null, null, null, null);
        when(chatEngine.chat(any(ChatRequest.class)))
                .thenReturn(ChatResponse.builder().success(true).content("牛顿第一定律：一切物体在没有受到力的作用时...").build());

        Map<String, Object> params = new HashMap<>();
        params.put("knowledge", knowledge);
        params.put("overallScore", 0.5);
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertTrue(output.isSuccess());
        assertTrue((Boolean) output.getData("teachDone"));
        String content = output.getData("teachContent");
        assertNotNull(content);
        assertTrue(content.contains("牛顿"));
    }

    @Test
    void testTeachMissingKnowledge() throws Exception {
        Map<String, Object> params = new HashMap<>();
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertFalse(output.isSuccess());
        assertTrue(output.getMsg().contains("knowledge"));
    }

    @Test
    void testTeachLlmFailure() throws Exception {
        KnowledgeResponse knowledge = new KnowledgeResponse(1L, "PHYS_001", "牛顿定律", "经典力学基础", 3, "物理", null, null, null, null);
        when(chatEngine.chat(any(ChatRequest.class)))
                .thenReturn(ChatResponse.builder().success(false).errorMessage("LLM 服务不可用").build());

        Map<String, Object> params = new HashMap<>();
        params.put("knowledge", knowledge);
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertFalse(output.isSuccess());
        assertNotNull(output.getData("teachContent"));
    }
}
