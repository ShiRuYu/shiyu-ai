package com.shiyu.ai.memory.magma;

import com.shiyu.ai.runtime.ContextQuery;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class MagmaContextRetrievalAdapterTest {
    @Test
    void missingSubjectFailsClosed() {
        MemoryQueryPort memory = mock(MemoryQueryPort.class);
        MagmaContextRetrievalAdapter adapter = new MagmaContextRetrievalAdapter(memory);

        assertTrue(adapter.retrieve(new ContextQuery(1, 7, "magma", "progress", 5, Map.of())).isEmpty());
        verifyNoInteractions(memory);
    }

    @Test
    void subjectIsForwardedToMemoryQuery() {
        MemoryQueryPort memory = mock(MemoryQueryPort.class);
        when(memory.retrieve(any())).thenReturn(java.util.List.of());
        MagmaContextRetrievalAdapter adapter = new MagmaContextRetrievalAdapter(memory);

        adapter.retrieve(new ContextQuery(1, 7, "magma", "progress", 5,
                Map.of("subjectType", "USER", "subjectId", "7")));

        verify(memory).retrieve(argThat(query -> query.subjectType().equals("USER") && query.subjectId().equals("7")
                && query.tenantId() == 1 && query.namespace().equals("magma")));
    }
}
