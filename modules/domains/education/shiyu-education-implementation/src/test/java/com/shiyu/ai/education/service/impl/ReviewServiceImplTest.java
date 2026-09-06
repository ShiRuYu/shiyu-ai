package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.ReviewTaskBO;
import com.shiyu.ai.education.dto.ReviewTaskResponse;
import com.shiyu.ai.education.port.repository.ReviewTaskRepository;
import com.shiyu.ai.education.request.ReviewRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class ReviewServiceImplTest {

    private static final ActorContext ACTOR = new ActorContext(new TenantId(9), new UserId(7), false);
    private final ReviewTaskRepository repository = mock(ReviewTaskRepository.class);
    private final ReviewServiceImpl service = new ReviewServiceImpl(repository);

    @Test
    void listsTasksWithActorTenant() {
        ReviewTaskBO task = new ReviewTaskBO();
        when(repository.selectById(ACTOR.tenantId(), 1L)).thenReturn(task);
        when(repository.selectTodayTasks(ACTOR.tenantId(), 10L)).thenReturn(List.of(task));
        when(repository.selectByStudentAndStatus(ACTOR.tenantId(), 10L, 0)).thenReturn(List.of(task));
        when(repository.selectByStudentAndKnowledge(ACTOR.tenantId(), 10L, 20L)).thenReturn(List.of(task));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            ReviewTaskResponse response = mock(ReviewTaskResponse.class);
            mapper.when(() -> MapstructUtils.convert(any(ReviewTaskBO.class), eq(ReviewTaskResponse.class))).thenReturn(response);
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(ReviewTaskResponse.class))).thenReturn(List.of(response));
            assertEquals(response, service.getById(ACTOR, 1L));
            assertEquals(1, service.listTodayTasks(ACTOR, 10L).size());
            assertEquals(1, service.listByStudentAndStatus(ACTOR, 10L, 0).size());
            assertEquals(1, service.listByStudentAndKnowledge(ACTOR, 10L, 20L).size());
        }
    }

    @Test
    void createsUpdatesCompletesAndDeletesTasks() {
        ReviewRequest request = new ReviewRequest();
        request.setStudentId(10L);
        request.setKnowledgeId(20L);
        when(repository.insert(eq(ACTOR.tenantId()), any(ReviewTaskBO.class))).thenReturn(1);
        ReviewTaskResponse response = mock(ReviewTaskResponse.class);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(ReviewTaskBO.class), eq(ReviewTaskResponse.class))).thenReturn(response);
            assertEquals(response, service.create(ACTOR, request));
        }

        ReviewTaskBO task = new ReviewTaskBO();
        task.setId(1L);
        when(repository.selectById(ACTOR.tenantId(), 1L)).thenReturn(task).thenReturn(task).thenReturn(null);
        request.setId(1L);
        request.setStatus(1);
        service.update(ACTOR, request);
        verify(repository).update(ACTOR.tenantId(), task);
        service.complete(ACTOR, 1L, 88.0);
        assertEquals(2, task.getStatus());
        assertEquals(88.0, task.getResultScore());
        verify(repository, org.mockito.Mockito.times(2)).update(ACTOR.tenantId(), task);
        service.complete(ACTOR, 1L, 90.0);
        service.delete(ACTOR, 1L);
        verify(repository).deleteById(ACTOR.tenantId(), 1L);

        ReviewRequest explicit = new ReviewRequest();
        explicit.setStudentId(12L); explicit.setKnowledgeId(22L); explicit.setReviewDate(LocalDate.of(2025, 1, 2));
        explicit.setReviewRound(3); explicit.setStatus(2); explicit.setResultScore(77.0);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(ReviewTaskBO.class), eq(ReviewTaskResponse.class)))
                    .thenReturn(mock(ReviewTaskResponse.class));
            service.create(ACTOR, explicit);
        }
        ReviewRequest allFields = new ReviewRequest();
        allFields.setId(1L); allFields.setStudentId(13L); allFields.setKnowledgeId(23L);
        allFields.setStatus(1); allFields.setReviewRound(4); allFields.setReviewDate(LocalDate.of(2025, 1, 3)); allFields.setResultScore(80.0);
        when(repository.selectById(ACTOR.tenantId(), 1L)).thenReturn(task);
        service.update(ACTOR, allFields);
        verify(repository, org.mockito.Mockito.atLeast(3)).update(ACTOR.tenantId(), task);
        when(repository.selectById(ACTOR.tenantId(), 99L)).thenReturn(null);
        ReviewRequest missing = new ReviewRequest(); missing.setId(99L);
        service.update(ACTOR, missing);
    }
}
