package com.shiyu.ai.aiagent.education;

import com.shiyu.ai.core.ChatEngine;
import com.shiyu.ai.core.ChatRequest;
import com.shiyu.ai.core.ChatResponse;
import com.shiyu.ai.education.ability.AbilityService;
import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TeacherAgent 单元测试
 */
@ExtendWith(MockitoExtension.class)
class TeacherAgentTest {

    @Mock
    private ChatEngine chatEngine;
    @Mock
    private KnowledgeService knowledgeService;
    @Mock
    private KnowledgeRelationService knowledgeRelationService;
    @Mock
    private AbilityService abilityService;

    private TeacherAgent teacherAgent;

    @BeforeEach
    void setUp() {
        teacherAgent = new TeacherAgent(chatEngine, knowledgeService,
                knowledgeRelationService, abilityService);
    }

    @Test
    void testTeach_Success() {
        // Arrange
        Long studentId = 1L;
        Long knowledgeId = 100L;

        KnowledgeResponse knowledge = new KnowledgeResponse(
                knowledgeId, "MATH001", "绝对值", "一个数在数轴上到原点的距离",
                2, "MATH", "basic", List.of(), List.of(), List.of());
        when(knowledgeService.getById(knowledgeId)).thenReturn(knowledge);

        when(knowledgeRelationService.getPrerequisites(knowledgeId))
                .thenReturn(List.of());

        AbilityValue ability = new AbilityValue(studentId, knowledgeId,
                50, 40, 30, 20, 10, 5, LocalDateTime.now());
        when(abilityService.get(studentId, knowledgeId)).thenReturn(ability);

        when(chatEngine.chat(any(ChatRequest.class)))
                .thenReturn(new ChatResponse(true,
                        "绝对值是一个数到原点的距离...",
                        "deepseek", "deepseek-v4", null));

        // Act
        TeacherAgent.TeachResponse response = teacherAgent.teach(studentId, knowledgeId);

        // Assert
        assertNotNull(response);
        assertEquals("绝对值", response.knowledge().name());
        assertTrue(response.content().contains("绝对值"));
        assertNotNull(response.ability());
        assertEquals(50, response.ability().remember());

        verify(knowledgeService).getById(knowledgeId);
        verify(knowledgeRelationService).getPrerequisites(knowledgeId);
        verify(abilityService).get(studentId, knowledgeId);
        verify(chatEngine).chat(any(ChatRequest.class));
    }

    @Test
    void testTeach_KnowledgeNotFound() {
        Long studentId = 1L;
        Long knowledgeId = 999L;

        when(knowledgeService.getById(knowledgeId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> teacherAgent.teach(studentId, knowledgeId));
    }

    @Test
    void testTeach_LlmFailure() {
        Long studentId = 1L;
        Long knowledgeId = 100L;

        KnowledgeResponse knowledge = new KnowledgeResponse(
                knowledgeId, "MATH001", "绝对值",
                null, 2, "MATH", null, List.of(), List.of(), List.of());
        when(knowledgeService.getById(knowledgeId)).thenReturn(knowledge);
        when(knowledgeRelationService.getPrerequisites(knowledgeId))
                .thenReturn(List.of());
        when(abilityService.get(studentId, knowledgeId))
                .thenReturn(AbilityValue.empty(studentId, knowledgeId));
        when(chatEngine.chat(any(ChatRequest.class)))
                .thenReturn(new ChatResponse(false, null, "deepseek", "deepseek-v4", "API 不可用"));

        assertThrows(RuntimeException.class,
                () -> teacherAgent.teach(studentId, knowledgeId));
    }
}
