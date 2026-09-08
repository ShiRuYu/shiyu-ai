package com.shiyu.ai.conversation.web;

import com.shiyu.ai.kernel.context.TenantId;

import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.GenerationAdmission;
import com.shiyu.ai.conversation.port.GenerationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GenerationControllerTest {
    private final GenerationRepository generations = mock(GenerationRepository.class);
    private final GenerationAdmission admission = mock(GenerationAdmission.class);
    private final GenerationController controller = new GenerationController(generations, admission);
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
    void clear() { UserContextHolder.clearContext(); }

    @Test
    void streamsDurableEventsAndHonorsLastEventId() {
        GenerationRun run = generation(GenerationStatus.RUNNING);
        GenerationEvent event = new GenerationEvent("g1", 2, GenerationEventType.DELTA, "hello", now);
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
        when(generations.update(any(GenerationRun.class), eq(0L))).thenReturn(1);
        when(generations.nextEventSequence("g1", new TenantId(7))).thenReturn(3);

        assertTrue(controller.cancel("g1").isSuccess());
        verify(generations).appendEvent(argThat(event -> event.type() == GenerationEventType.CANCELLED && event.sequence() == 3), eq(new TenantId(7L)));
        verify(admission).release(any(), argThat(cancelled -> cancelled.status() == GenerationStatus.CANCELLED));
    }

    @Test
    void rejectsUnknownAndNotRunningGeneration() {
        when(generations.find("missing", new TenantId(7), 8)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> controller.cancel("missing"));
        GenerationRun created = generation(GenerationStatus.CREATED);
        when(generations.find("g2", new TenantId(7), 8)).thenReturn(Optional.of(created));
        assertThrows(RuntimeException.class, () -> controller.cancel("g2"));
    }

    private GenerationRun generation(GenerationStatus status) {
        return new GenerationRun("g1", "c1", "m1", null, null, "OPENAI", "gpt", status,
                0, 0, 0, null, -1, false, 0, now, now);
    }
}
