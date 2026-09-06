package com.shiyu.ai.education.agent;

import com.shiyu.ai.education.domain.model.ExamBO;
import com.shiyu.ai.education.domain.model.StudyPlanBO;
import com.shiyu.ai.education.port.repository.ExamRepository;
import com.shiyu.ai.education.port.repository.StudyPlanItemRepository;
import com.shiyu.ai.education.port.repository.StudyPlanRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.port.KnowledgePathPort;
import com.shiyu.ai.knowledge.port.KnowledgePointPort;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EducationAgentTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(1L), new UserId(2L), false);
    private static final KnowledgeResponse KNOWLEDGE = new KnowledgeResponse(
            10L, "MATH", "函数", "函数概念", 2, "数学", "[]", List.of(), List.of(), List.of());

    @Test
    void examAgentCollectsKnowledgePersistsAndSurfacesLlmFailure() {
        ChatEngine chat = mock(ChatEngine.class);
        KnowledgePointPort points = mock(KnowledgePointPort.class);
        com.shiyu.ai.education.service.ExamService service = mock(com.shiyu.ai.education.service.ExamService.class);
        ExamRepository repo = mock(ExamRepository.class);
        when(points.getResponse(ACTOR, 10L)).thenReturn(KNOWLEDGE);
        when(points.getResponse(ACTOR, 11L)).thenThrow(new RuntimeException("missing"));
        when(chat.chat(any())).thenReturn(ChatResponse.builder().success(true).content("ok").build());
        doAnswer(invocation -> { ((ExamBO) invocation.getArgument(1)).setId(99L); return 1; })
                .when(repo).insert(eq(new TenantId(1L)), any(ExamBO.class));

        ExamAgent agent = new ExamAgent(chat, points, service, repo);
        ExamBO exam = agent.generateExam(ACTOR, "MATH", 8, List.of(10L, 11L), 45, 7L);
        assertEquals(99L, exam.getId());
        assertEquals("MATH", exam.getSubjectCode());
        assertEquals(100, exam.getTotalScore());
        verify(repo).insert(eq(new TenantId(1L)), any(ExamBO.class));

        when(chat.chat(any())).thenReturn(ChatResponse.builder().success(false).errorMessage("down").build());
        assertThrows(RuntimeException.class, () -> agent.generateExam(ACTOR, "MATH", 8, List.of(), 30, 7L));
    }

    @Test
    void plannerAgentValidatesTargetAndDistributesDailyItems() {
        ChatEngine chat = mock(ChatEngine.class);
        KnowledgePointPort points = mock(KnowledgePointPort.class);
        KnowledgePathPort paths = mock(KnowledgePathPort.class);
        StudyPlanRepository plans = mock(StudyPlanRepository.class);
        StudyPlanItemRepository items = mock(StudyPlanItemRepository.class);
        when(points.getResponse(ACTOR, 10L)).thenReturn(KNOWLEDGE);
        when(paths.generatePath(ACTOR, 10L)).thenReturn(List.of(8L, 9L, 10L));
        doAnswer(invocation -> { ((StudyPlanBO) invocation.getArgument(1)).setId(55L); return 1; })
                .when(plans).insert(eq(new TenantId(1L)), any(StudyPlanBO.class));

        PlannerAgent agent = new PlannerAgent(chat, points, paths, plans, items);
        StudyPlanBO result = agent.generatePlan(ACTOR, 2L, 10L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
        assertEquals(55L, result.getId());
        assertEquals(0, result.getStatus());
        verify(items).insertBatch(eq(new TenantId(1L)), argThat(list -> list.size() == 3));

        when(points.getResponse(ACTOR, 99L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> agent.generatePlan(ACTOR, 2L, 99L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2)));
    }
}
