package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.education.domain.LearningState;
import com.shiyu.ai.education.domain.model.LearningStateBO;
import com.shiyu.ai.education.port.repository.LearningStateRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LearningStateMachineImplTest {

    private static final ActorContext ACTOR = new ActorContext(new TenantId(9), new UserId(7), false);
    private final LearningStateRepository repository = mock(LearningStateRepository.class);
    private final LearningStateMachineImpl machine = new LearningStateMachineImpl(repository);

    @Test
    void missingOrInvalidPersistedStateFailsClosedToNotStarted() {
        when(repository.selectByStudentAndKnowledge(ACTOR.tenantId(), 10L, 20L)).thenReturn(null);
        assertEquals(LearningState.NOT_STARTED, machine.getState(ACTOR, 10L, 20L));

        LearningStateBO invalid = new LearningStateBO();
        invalid.setState("REMOVED");
        when(repository.selectByStudentAndKnowledge(ACTOR.tenantId(), 10L, 20L)).thenReturn(invalid);
        assertEquals(LearningState.NOT_STARTED, machine.getState(ACTOR, 10L, 20L));
    }

    @Test
    void readsPersistedStateAndPersistsEveryTransitionWithTenant() {
        LearningStateBO state = new LearningStateBO();
        state.setState(LearningState.FORGOTTEN.name());
        when(repository.selectByStudentAndKnowledge(ACTOR.tenantId(), 10L, 20L)).thenReturn(state);

        assertEquals(LearningState.FORGOTTEN, machine.getState(ACTOR, 10L, 20L));
        machine.startLearning(ACTOR, 10L, 20L);

        var captured = org.mockito.ArgumentCaptor.forClass(LearningStateBO.class);
        verify(repository).upsert(eq(ACTOR.tenantId()), captured.capture());
        assertEquals(10L, captured.getValue().getStudentId());
        assertEquals(20L, captured.getValue().getKnowledgeId());
        assertEquals(LearningState.LEARNING.name(), captured.getValue().getState());
    }

    @Test
    void delegatesAllCommandsToTheStateMachine() {
        when(repository.selectByStudentAndKnowledge(any(), eq(10L), eq(20L))).thenReturn(null);

        machine.startLearning(ACTOR, 10L, 20L);
        machine.passAssessment(ACTOR, 10L, 20L);
        machine.deepPractice(ACTOR, 10L, 20L);
        machine.forget(ACTOR, 10L, 20L);
        machine.scheduleReview(ACTOR, 10L, 20L);
        machine.giveUp(ACTOR, 10L, 20L);

        org.mockito.Mockito.verify(repository, org.mockito.Mockito.times(6))
                .upsert(eq(ACTOR.tenantId()), any(LearningStateBO.class));
    }
}
