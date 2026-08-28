package com.shiyu.ai.conversation.web;

import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.GenerationAdmission;
import com.shiyu.ai.conversation.port.GenerationRepository;
import com.shiyu.ai.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "varargs"})
class GenerationControllerRuntimeTest {
    private final GenerationRepository generations = mock(GenerationRepository.class);
    private final GenerationAdmission admission = mock(GenerationAdmission.class);
    private final AiRuntimePort runtime = mock(AiRuntimePort.class);
    private final GenerationController controller = new GenerationController(generations, admission, runtime);
    private final Instant now = Instant.now();

    @BeforeEach
    void actor() {
        UserContext context = new UserContext(); context.setUserId(8L); context.setCurrentTenantId(7L); context.setHomeTenantId(7L);
        UserContextHolder.setContext(context);
    }

    @AfterEach
    void clear() { UserContextHolder.clearContext(); }

    @Test
    void streamsRuntimeProjectionAndMapsTerminalEvents() {
        GenerationRun generation = generation(GenerationStatus.RUNNING);
        AiRun run = new AiRun("r1", new TenantId(7), new UserId(8), null, null, AiRunSource.GENERATION, "g1", null, null,
                "c1", "g1", null, "gpt", "hash", AiRunStatus.RUNNING, 0, 0, false, null, now, null, null, 0);
        AiRunEvent event = new AiRunEvent("r1", new TenantId(7), 2, AiRunEventType.RUN_COMPLETED, 1, null, null, null,
                null, "g1", null, null, null, null, null, "{}", true, now);
        when(generations.find("g1", new TenantId(7), 8)).thenReturn(Optional.of(generation));
        when(runtime.requireGenerationRun("g1", new TenantId(7), 8)).thenReturn(run);
        when(runtime.events("r1", new TenantId(7), 8, 1, 1000)).thenReturn(List.of(event));
        var result = controller.stream("g1", -1, false, 1000, "1").collectList().block();
        assertNotNull(result); assertEquals(1, result.size());
        assertEquals(GenerationEventType.COMPLETED, result.getFirst().data().type());
        assertEquals("g1", result.getFirst().data().generationRunId());
    }

    @Test
    void cancelsThroughRuntimeAndRejectsCasConflict() {
        GenerationRun generation = generation(GenerationStatus.RUNNING);
        AiRun run = new AiRun("r1", new TenantId(7), new UserId(8), null, null, AiRunSource.GENERATION, "g1", null, null,
                "c1", "g1", null, "gpt", "hash", AiRunStatus.RUNNING, 0, 0, false, null, now, null, null, 0);
        when(generations.find("g1", new TenantId(7), 8)).thenReturn(Optional.of(generation));
        when(generations.update(any(GenerationRun.class), eq(0L))).thenReturn(1);
        when(runtime.requireGenerationRun("g1", new TenantId(7), 8)).thenReturn(run);
        assertTrue(controller.cancel("g1").isSuccess());
        verify(runtime).finish("r1", new TenantId(7), 8, AiRunStatus.CANCELLED, "CLIENT_CANCELLED");
        verify(generations, never()).appendEvent(any(), any(TenantId.class));

        when(generations.update(any(GenerationRun.class), eq(0L))).thenReturn(0);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.cancel("g1"));
    }

    @Test
    void rejectsMissingRuntimeProjectionInsteadOfMaskingTheFailure() {
        GenerationRun generation = generation(GenerationStatus.RUNNING);
        when(generations.find("g1", new TenantId(7), 8)).thenReturn(Optional.of(generation));
        when(runtime.requireGenerationRun("g1", new TenantId(7), 8))
                .thenThrow(new IllegalStateException("projection missing"));
        assertThrows(IllegalStateException.class,
                () -> controller.stream("g1", -1, false, 1000, "not-a-number"));
        verify(generations, never()).listEvents(anyString(), any(TenantId.class), anyInt(), anyInt());
    }

    @Test
    void followsRuntimeWithHeartbeatAndStopsOnTerminalEvent() {
        GenerationRun generation = generation(GenerationStatus.RUNNING);
        AiRun run = new AiRun("r1", new TenantId(7), new UserId(8), null, null, AiRunSource.GENERATION, "g1", null, null,
                "c1", "g1", null, "gpt", "hash", AiRunStatus.RUNNING, 0, 0, false, null, now, null, null, 0);
        AiRunEvent terminal = new AiRunEvent("r1", new TenantId(7), 2, AiRunEventType.RUN_FAILED, 2, null, null, null,
                null, "g1", null, null, null, null, null, "failed", true, now);
        when(generations.find("g1", new TenantId(7), 8)).thenReturn(Optional.of(generation));
        when(runtime.requireGenerationRun("g1", new TenantId(7), 8)).thenReturn(run);
        when(runtime.events(eq("r1"), eq(new TenantId(7L)), eq(8L), anyLong(), eq(1000)))
                .thenReturn(List.of(), List.of(terminal));
        var result = controller.stream("g1", -1, true, 1000, null)
                .take(2).collectList().block(Duration.ofSeconds(3));
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.getFirst().data() == null || result.get(1).data().type() == GenerationEventType.FAILED);
    }

    @Test
    void mapsEveryRuntimeEventAndFallsBackToRunId() throws Exception {
        Method mapper = GenerationController.class.getDeclaredMethod("projectRuntimeEvent", AiRunEvent.class);
        mapper.setAccessible(true);
        for (AiRunEventType type : AiRunEventType.values()) {
            AiRunEvent event = new AiRunEvent("r1", new TenantId(7), 1, type, 3, null, null, null,
                    null, type == AiRunEventType.RUN_STARTED ? null : "g1", null, null, null, null, null, "payload", true, now);
            GenerationEvent projected = (GenerationEvent) mapper.invoke(controller, event);
            assertNotNull(projected);
            assertEquals(type == AiRunEventType.RUN_STARTED ? "r1" : "g1", projected.generationRunId());
        }
        Method terminal = GenerationController.class.getDeclaredMethod("isTerminal", GenerationEventType.class);
        terminal.setAccessible(true);
        assertTrue((Boolean) terminal.invoke(controller, GenerationEventType.COMPLETED));
        assertTrue((Boolean) terminal.invoke(controller, GenerationEventType.FAILED));
        assertTrue((Boolean) terminal.invoke(controller, GenerationEventType.CANCELLED));
        assertFalse((Boolean) terminal.invoke(controller, GenerationEventType.DELTA));
    }

    private GenerationRun generation(GenerationStatus status) {
        return new GenerationRun("g1", "c1", "m1", null, null, "OPENAI", "gpt", status,
                0, 0, 0, null, -1, false, 0, now, now);
    }
}
