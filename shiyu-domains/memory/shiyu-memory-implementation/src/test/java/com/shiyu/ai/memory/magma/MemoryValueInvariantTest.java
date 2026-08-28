package com.shiyu.ai.memory.magma;

import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MemoryValueInvariantTest {
    @Test
    void normalizesOptionalCollectionsAndRejectsInvalidEntities() {
        MemoryEntity entity = new MemoryEntity("e1", new TenantId(7L), "USER", "u1", "User", null, null, true);
        assertEquals(Map.of(), entity.attributes());
        assertThrows(IllegalArgumentException.class, () -> new MemoryEntity("", new TenantId(7L), "USER", "u1", "", "", Map.of(), true));
        assertThrows(IllegalArgumentException.class, () -> new MemoryEntity("e1", new TenantId(7L), "", "u1", "", "", Map.of(), true));
        assertThrows(IllegalArgumentException.class, () -> new MemoryEntity("e1", new TenantId(7L), "USER", "", "", "", Map.of(), true));
    }

    @Test
    void normalizesTraceAndResultDefaults() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        MemoryRetrievalTrace trace = new MemoryRetrievalTrace("t1", new TenantId(7L), "default", "q", null,
                Map.of(GraphType.ENTITY, 0.5d), Arrays.asList(List.of("a"), null), null, null, now);
        assertEquals(List.of(), trace.anchorEventIds());
        assertEquals(List.of(), trace.resultEventIds());
        assertEquals(List.of(List.of("a"), List.of()), trace.relationPaths());
        assertEquals(now, trace.createdAt());

        MemoryRetrievalTrace shorthand = new MemoryRetrievalTrace("t2", new TenantId(7L), "default", "q", List.of("e"), now);
        assertEquals(List.of("e"), shorthand.resultEventIds());
        assertEquals(List.of(), new MemoryRetrievalResult(null, "trace").paths());
    }
}
