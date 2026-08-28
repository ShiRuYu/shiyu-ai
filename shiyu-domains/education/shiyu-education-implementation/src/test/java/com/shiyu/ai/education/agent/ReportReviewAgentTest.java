package com.shiyu.ai.education.agent;

import com.shiyu.ai.education.domain.ReviewScheduler;
import com.shiyu.ai.education.domain.model.ReviewTaskBO;
import com.shiyu.ai.education.domain.model.StudyRecordBO;
import com.shiyu.ai.education.domain.enums.ReviewTaskStatus;
import com.shiyu.ai.education.port.repository.ReviewTaskRepository;
import com.shiyu.ai.education.port.repository.StudyRecordRepository;
import com.shiyu.ai.education.service.AbilityService;
import com.shiyu.ai.education.service.AnalyticsService;
import com.shiyu.ai.education.service.ReviewService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReportReviewAgentTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(7), new UserId(11), false);

    @Test
    void generatesSuccessfulAndFallbackLearningReports() {
        ChatEngine chat = mock(ChatEngine.class);
        StudyRecordRepository records = mock(StudyRecordRepository.class);
        StudyRecordBO first = new StudyRecordBO(); first.setScore(80d); first.setDurationSec(120);
        StudyRecordBO second = new StudyRecordBO(); second.setScore(null); second.setDurationSec(null);
        when(records.selectByStudent(ACTOR.tenantId(), 11L)).thenReturn(List.of(first, second));
        when(chat.chat(any())).thenReturn(ChatResponse.builder().success(true).content("report").build());
        ReportAgent agent = new ReportAgent(chat, mock(AnalyticsService.class), records, mock(AbilityService.class));
        assertEquals("report", agent.generateOverviewReport(ACTOR, 11L));

        when(chat.chat(any())).thenReturn(ChatResponse.builder().success(false).errorMessage("down").build());
        String fallback = agent.generateOverviewReport(ACTOR, 11L);
        assertTrue(fallback.contains("学习次数：2"));
        assertTrue(fallback.contains("总学习时长：2 分钟"));
    }

    @Test
    void schedulesCompletesAndFiltersOverdueReviewTasks() {
        ReviewTaskRepository tasks = mock(ReviewTaskRepository.class);
        ReviewScheduler scheduler = mock(ReviewScheduler.class);
        when(scheduler.scheduleAfterLearning(eq(11L), eq(22L), any(Instant.class))).thenReturn(List.of(
                new ReviewScheduler.ReviewTask(11L, 22L, LocalDate.now().minusDays(1), 1),
                new ReviewScheduler.ReviewTask(11L, 22L, LocalDate.now().plusDays(2), 2)));
        ReviewAgent agent = new ReviewAgent(scheduler, mock(ReviewService.class), tasks);
        List<ReviewTaskBO> saved = agent.scheduleAfterLearning(ACTOR, 11L, 22L);
        assertEquals(2, saved.size());
        verify(tasks, times(2)).insert(eq(ACTOR.tenantId()), any(ReviewTaskBO.class));

        ReviewTaskBO task = new ReviewTaskBO(); task.setId(3L); task.setStatus(ReviewTaskStatus.PENDING.getCode());
        when(tasks.selectById(ACTOR.tenantId(), 3L)).thenReturn(task);
        assertSame(task, agent.completeReview(ACTOR, 3L, 91d));
        assertEquals(ReviewTaskStatus.COMPLETED.getCode(), task.getStatus());
        assertThrows(IllegalArgumentException.class, () -> agent.completeReview(ACTOR, 99L, 1d));

        ReviewTaskBO overdue = new ReviewTaskBO(); overdue.setReviewDate(LocalDate.now().minusDays(1));
        ReviewTaskBO future = new ReviewTaskBO(); future.setReviewDate(LocalDate.now().plusDays(1));
        ReviewTaskBO undated = new ReviewTaskBO();
        when(tasks.selectByStudentAndStatus(ACTOR.tenantId(), 11L, ReviewTaskStatus.PENDING.getCode()))
                .thenReturn(List.of(overdue, future, undated));
        assertEquals(List.of(overdue), agent.getOverdueTasks(ACTOR, 11L));
        assertEquals(List.of(), agent.getTodayTasks(ACTOR, 11L));
    }
}
