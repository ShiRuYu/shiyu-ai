package com.shiyu.ai.aiagent.education;

import com.shiyu.ai.core.ChatEngine;
import com.shiyu.ai.core.ChatRequest;
import com.shiyu.ai.core.ChatResponse;
import com.shiyu.ai.dal.dataobject.education.QuestionDO;
import com.shiyu.ai.education.ability.AbilityService;
import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.education.domain.DifficultyLevel;
import com.shiyu.ai.education.question.QuestionService;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
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
 * PracticeAgent 单元测试
 */
@ExtendWith(MockitoExtension.class)
class PracticeAgentTest {

    @Mock
    private ChatEngine chatEngine;
    @Mock
    private KnowledgeService knowledgeService;
    @Mock
    private AbilityService abilityService;
    @Mock
    private QuestionService questionService;

    private PracticeAgent practiceAgent;

    @BeforeEach
    void setUp() {
        practiceAgent = new PracticeAgent(chatEngine, knowledgeService,
                abilityService, questionService);
    }

    @Test
    void testGenerate_Success() {
        // Arrange
        Long studentId = 1L;
        Long knowledgeId = 100L;
        int count = 3;

        KnowledgeResponse knowledge = new KnowledgeResponse(
                knowledgeId, "MATH001", "绝对值", "一个数到原点的距离",
                2, "MATH", null, List.of(), List.of(), List.of());
        when(knowledgeService.getById(knowledgeId)).thenReturn(knowledge);

        AbilityValue ability = new AbilityValue(studentId, knowledgeId,
                50, 40, 30, 20, 10, 5, LocalDateTime.now());
        when(abilityService.get(studentId, knowledgeId)).thenReturn(ability);

        String llmResponse = """
                {"type":"CHOICE","title":"-5的绝对值是多少？","options":["A. 5","B. -5","C. 0","D. 1"],"answer":"A","analysis":"绝对值是数到原点的距离","ability_dimension":"remember"}
                {"type":"FILL","title":"|3|=___","options":null,"answer":"3","analysis":"正数的绝对值等于本身","ability_dimension":"remember"}
                """;
        when(chatEngine.chat(any(ChatRequest.class)))
                .thenReturn(new ChatResponse(true, llmResponse, "deepseek", "deepseek-v4", null));

        when(questionService.create(any(QuestionDO.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<QuestionDO> questions = practiceAgent.generate(studentId, knowledgeId, count);

        // Assert
        assertNotNull(questions);
        assertTrue(questions.size() > 0);
        verify(knowledgeService).getById(knowledgeId);
        verify(abilityService).get(studentId, knowledgeId);
        verify(chatEngine).chat(any(ChatRequest.class));
    }

    @Test
    void testGenerate_KnowledgeNotFound() {
        Long studentId = 1L;
        Long knowledgeId = 999L;

        when(knowledgeService.getById(knowledgeId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> practiceAgent.generate(studentId, knowledgeId, 5));
    }

    @Test
    void testGenerate_LlmFailure() {
        Long studentId = 1L;
        Long knowledgeId = 100L;

        KnowledgeResponse knowledge = new KnowledgeResponse(
                knowledgeId, "MATH001", "绝对值", null,
                2, "MATH", null, List.of(), List.of(), List.of());
        when(knowledgeService.getById(knowledgeId)).thenReturn(knowledge);
        when(abilityService.get(studentId, knowledgeId))
                .thenReturn(AbilityValue.empty(studentId, knowledgeId));
        when(chatEngine.chat(any(ChatRequest.class)))
                .thenReturn(new ChatResponse(false, null, "deepseek", "deepseek-v4", "API Error"));

        assertThrows(RuntimeException.class,
                () -> practiceAgent.generate(studentId, knowledgeId, 5));
    }
}
