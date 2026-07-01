package com.shiyu.ai.aiagent.education;

import com.shiyu.ai.dal.dataobject.education.ReviewTaskDO;
import com.shiyu.ai.education.domain.ReviewStatus;
import com.shiyu.ai.education.review.ReviewService;
import com.shiyu.ai.knowledge.review.ReviewScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ReviewAgent 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ReviewAgentTest {

    @Mock
    private ReviewScheduler reviewScheduler;
    @Mock
    private ReviewService reviewService;

    private ReviewAgent reviewAgent;

    @BeforeEach
    void setUp() {
        reviewAgent = new ReviewAgent(reviewScheduler, reviewService);
    }

    @Test
    void testScheduleAfterLearning() {
        Long studentId = 1L;
        Long knowledgeId = 100L;

        when(reviewScheduler.scheduleAfterLearning(any(), any(), any()))
                .thenReturn(List.of(
                        new ReviewScheduler.ReviewTask(studentId, knowledgeId, LocalDate.now().plusDays(1), 1),
                        new ReviewScheduler.ReviewTask(studentId, knowledgeId, LocalDate.now().plusDays(3), 2)
                ));

        when(reviewService.create(any(ReviewTaskDO.class)))
                .thenAnswer(invocation -> {
                    ReviewTaskDO task = invocation.getArgument(0);
                    task.setId(1L);
                    return task;
                });

        List<ReviewTaskDO> tasks = reviewAgent.scheduleAfterLearning(studentId, knowledgeId);

        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        verify(reviewScheduler).scheduleAfterLearning(any(), any(), any());
        verify(reviewService, times(2)).create(any(ReviewTaskDO.class));
    }

    @Test
    void testGetTodayTasks() {
        Long studentId = 1L;
        ReviewTaskDO task = new ReviewTaskDO();
        task.setId(1L);
        task.setStudentId(studentId);
        task.setStatus(ReviewStatus.PENDING.name());

        when(reviewService.listTodayTasks(studentId))
                .thenReturn(List.of(task));

        List<ReviewTaskDO> tasks = reviewAgent.getTodayTasks(studentId);

        assertEquals(1, tasks.size());
        assertEquals(studentId, tasks.get(0).getStudentId());
    }

    @Test
    void testCompleteReview() {
        Long taskId = 1L;
        Double score = 85.0;

        ReviewTaskDO task = new ReviewTaskDO();
        task.setId(taskId);
        task.setStatus(ReviewStatus.PENDING.name());
        when(reviewService.getById(taskId)).thenReturn(task);

        reviewAgent.completeReview(taskId, score);

        assertEquals(ReviewStatus.COMPLETED.name(), task.getStatus());
        assertEquals(score, task.getResultScore());
        assertNotNull(task.getCompletedAt());
        verify(reviewService).update(task);
    }
}
