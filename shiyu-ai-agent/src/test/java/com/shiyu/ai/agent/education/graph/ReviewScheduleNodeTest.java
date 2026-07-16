package com.shiyu.ai.agent.education.graph;

import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.dal.bo.education.ReviewTaskBO;
import com.shiyu.ai.dal.repository.education.ReviewTaskRepository;
import com.shiyu.ai.education.domain.ReviewScheduler;
import com.shiyu.ai.education.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Tag("dev")
@ExtendWith(MockitoExtension.class)
class ReviewScheduleNodeTest {

    @Mock private ReviewScheduler reviewScheduler;
    @Mock private ReviewService reviewService;
    @Mock private ReviewTaskRepository reviewTaskRepository;
    @Captor private ArgumentCaptor<ReviewTaskBO> taskCaptor;

    private ReviewScheduleNode node;

    @BeforeEach
    void setUp() {
        node = new ReviewScheduleNode(reviewScheduler, reviewService, reviewTaskRepository);
    }

    @Test
    void testScheduleReviewSuccess() throws Exception {
        List<ReviewScheduler.ReviewTask> scheduledTasks = List.of(
                new ReviewScheduler.ReviewTask(100L, 1L, LocalDate.now().plusDays(1), 1),
                new ReviewScheduler.ReviewTask(100L, 1L, LocalDate.now().plusDays(7), 2),
                new ReviewScheduler.ReviewTask(100L, 1L, LocalDate.now().plusDays(16), 3)
        );
        when(reviewScheduler.scheduleAfterLearning(anyLong(), anyLong(), any())).thenReturn(scheduledTasks);

        Map<String, Object> params = new HashMap<>();
        params.put("studentId", 100L);
        params.put("knowledgeId", 1L);
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertTrue(output.isSuccess());
        assertEquals(3, ((Number) output.getData("reviewCount")).intValue());
        assertTrue((Boolean) output.getData("reviewScheduled"));
        verify(reviewTaskRepository, times(3)).insert(taskCaptor.capture());
    }

    @Test
    void testScheduleMissingStudentId() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("knowledgeId", 1L);
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertFalse(output.isSuccess());
    }

    @Test
    void testScheduleMissingKnowledgeId() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("studentId", 100L);
        NodeInput input = NodeInput.fromMap(params);

        NodeOutput output = node.doExecute(input);

        assertFalse(output.isSuccess());
    }
}
