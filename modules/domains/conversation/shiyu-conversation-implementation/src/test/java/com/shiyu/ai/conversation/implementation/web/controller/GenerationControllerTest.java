package com.shiyu.ai.conversation.implementation.web.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.common.foundation.context.model.UserContext;
import com.shiyu.ai.common.foundation.context.UserContextHolder;
import com.shiyu.ai.conversation.contract.api.*;
import com.shiyu.ai.conversation.contract.model.*;
import com.shiyu.ai.conversation.implementation.application.*;
import com.shiyu.ai.conversation.implementation.domain.model.*;
import com.shiyu.ai.conversation.implementation.domain.port.*;
import com.shiyu.ai.conversation.implementation.domain.port.GenerationRepository;
import com.shiyu.ai.kernel.context.TenantId;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 验证 生成 Controller 相关功能、边界条件、异常路径和协作行为。
 */
class GenerationControllerTest {
    private final GenerationRepository generations = mock(GenerationRepository.class);
    private final GenerationRunner runner = mock(GenerationRunner.class);
    private final ConversationRepository conversations = mock(ConversationRepository.class);
    private final GenerationController controller =
            new GenerationController(generations, null, runner, conversations);
    private final Instant now = Instant.now();

    @BeforeEach
    void actor() {
        UserContext context = new UserContext();
        context.setUserId(8L);
        context.setCurrentTenantId(7L);
        context.setHomeTenantId(7L);
        UserContextHolder.setContext(context);
    }

    @AfterEach
    void clear() {
        UserContextHolder.clearContext();
    }

    @Test
    void streamsDurableEventsAndHonorsLastEventId() {
        GenerationRun run = generation(GenerationStatus.RUNNING);
        GenerationEvent event =
                new GenerationEvent("g1", 2, GenerationEventType.DELTA, "hello", now);
        when(generations.find("g1", new TenantId(7), 8)).thenReturn(Optional.of(run));
        when(generations.listEvents("g1", new TenantId(7), 2, 1000)).thenReturn(List.of(event));

        var events = controller.stream("g1", -1, false, 1000, "2").collectList().block();
        assertTrue(events != null && events.size() == 1);
        assertTrue("2".equals(events.getFirst().id()));
        assertTrue(events.getFirst().data() == event);
    }

    @Test
    void cancelsRunningGenerationAndEmitsProjectionEvent() {
        GenerationRun run = generation(GenerationStatus.RUNNING);
        when(generations.find("g1", new TenantId(7), 8)).thenReturn(Optional.of(run));
        when(conversations.transitionRunningGeneration(
                        any(GenerationRun.class), eq(0L), eq(8L), anyList()))
                .thenReturn(1);

        assertTrue(controller.cancel("g1").isSuccess());
        verify(conversations)
                .transitionRunningGeneration(
                        argThat(
                                cancelled ->
                                        cancelled.status() == GenerationStatus.CANCELLED
                                                && cancelled.cancelRequested()
                                                && cancelled.version() == 1),
                        eq(0L),
                        eq(8L),
                        eq(List.of()));
        verify(runner).cancel("g1");
        verify(runner).processTerminalPostProcessing("g1");
        verify(generations, never()).update(any(GenerationRun.class), anyLong());
        verify(generations, never()).appendEvent(any(), any(TenantId.class));
    }

    @Test
    void rejectsUnknownAndNotRunningGeneration() {
        when(generations.find("missing", new TenantId(7), 8)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> controller.cancel("missing"));
        GenerationRun created = generation(GenerationStatus.CREATED);
        when(generations.find("g2", new TenantId(7), 8)).thenReturn(Optional.of(created));
        assertThrows(RuntimeException.class, () -> controller.cancel("g2"));

        when(generations.find("g3", new TenantId(7), 8))
                .thenReturn(Optional.of(generation(GenerationStatus.RUNNING)));
        when(conversations.transitionRunningGeneration(
                        any(GenerationRun.class), eq(0L), eq(8L), anyList()))
                .thenReturn(0);
        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> controller.cancel("g3"));
        verify(runner, never()).cancel("g3");
    }

    private GenerationRun generation(GenerationStatus status) {
        return new GenerationRun(
                "g1", "c1", "m1", null, null, "OPENAI", "gpt", status, 0, 0, 0, null, -1, false, 0,
                now, now);
    }
}
