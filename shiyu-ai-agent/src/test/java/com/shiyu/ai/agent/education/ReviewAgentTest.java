package com.shiyu.ai.agent.education;

import com.shiyu.ai.dal.dataobject.education.ReviewTaskDO;
import com.shiyu.ai.education.domain.ReviewScheduler;
import com.shiyu.ai.education.domain.ReviewStatus;
import com.shiyu.ai.education.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ReviewAgent 单元测试
 */
@Tag("dev")
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
        ReviewScheduler.ReviewTask task = new ReviewScheduler.ReviewTask(
                1L, 100L, LocalDate.now().plusDays(1), 1);

        when(reviewScheduler.scheduleAfterLearning(anyLong(), anyLong(), any(Instant.class)))
                .thenReturn(List.of(task));
        when(reviewService.create(any(ReviewTaskDO.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<ReviewTaskDO> results = reviewAgent.scheduleAfterLearning(1L, 100L);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getStudentId());
        assertEquals(ReviewStatus.PENDING.name(), results.get(0).getStatus());

        verify(reviewScheduler, times(1))
                .scheduleAfterLearning(anyLong(), anyLong(), any(Instant.class));
        verify(reviewService, times(1)).create(any(ReviewTaskDO.class));
    }

    @Test
    void testGetTodayTasks() {
        ReviewTaskDO task = new ReviewTaskDO();
        task.setId(1L);
        task.setStudentId(1L);
        task.setReviewDate(LocalDate.now());

        when(reviewService.listTodayTasks(1L)).thenReturn(List.of(task));

        List<ReviewTaskDO> results = reviewAgent.getTodayTasks(1L);

        assertEquals(1, results.size());
        verify(reviewService, times(1)).listTodayTasks(1L);
    }

    @Test
    void testCompleteReview() {
        ReviewTaskDO task = new ReviewTaskDO();
        task.setId(1L);
        task.setStatus(ReviewStatus.PENDING.name());

        when(reviewService.getById(1L)).thenReturn(task);

        ReviewTaskDO result = reviewAgent.completeReview(1L, 95.0);

        assertEquals(ReviewStatus.COMPLETED.name(), result.getStatus());
        assertEquals(95.0, result.getResultScore());
        assertNotNull(result.getCompletedAt());
        verify(reviewService, times(1)).update(any(ReviewTaskDO.class));
    }

    @Test
    void testCompleteReviewTaskNotFound() {
        when(reviewService.getById(999L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
            reviewAgent.completeReview(999L, 80.0)
        );
    }

    @Test
    void testGetOverdueTasks() {
        ReviewTaskDO overdue = new ReviewTaskDO();
        overdue.setId(1L);
        overdue.setReviewDate(LocalDate.now().minusDays(2));
        overdue.setStatus(ReviewStatus.PENDING.name());

        ReviewTaskDO future = new ReviewTaskDO();
        future.setId(2L);
        future.setReviewDate(LocalDate.now().plusDays(1));
        future.setStatus(ReviewStatus.PENDING.name());

        when(reviewService.listByStudentAndStatus(1L, ReviewStatus.PENDING.name()))
                .thenReturn(List.of(overdue, future));

        List<ReviewTaskDO> overdueTasks = reviewAgent.getOverdueTasks(1L);

        assertEquals(1, overdueTasks.size());
        assertEquals(1L, overdueTasks.get(0).getId());
    }

    @Test
    void testGetOverdueTasksNoOverdue() {
        ReviewTaskDO future = new ReviewTaskDO();
        future.setId(2L);
        future.setReviewDate(LocalDate.now().plusDays(1));
        future.setStatus(ReviewStatus.PENDING.name());

        when(reviewService.listByStudentAndStatus(1L, ReviewStatus.PENDING.name()))
                .thenReturn(List.of(future));

        List<ReviewTaskDO> overdueTasks = reviewAgent.getOverdueTasks(1L);

        assertTrue(overdueTasks.isEmpty());
    }
}
