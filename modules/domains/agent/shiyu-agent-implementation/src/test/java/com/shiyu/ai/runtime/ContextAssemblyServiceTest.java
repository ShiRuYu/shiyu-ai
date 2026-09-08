package com.shiyu.ai.runtime;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContextAssemblyServiceTest {
    @Test
    void filtersUnauthorizedItemsAndAppliesTopK() {
        ContextRetrievalPort provider = query -> List.of(
                new ContextItem("knowledge", "allowed", "A", 0.9, null, List.of("doc"), "tenant:1", Instant.now()),
                new ContextItem("magma", "denied", "B", 1.0, null, List.of("edge"), "", Instant.now()),
                new ContextItem("magma", "second", "C", 0.8, null, List.of("edge"), "tenant:1", Instant.now())
        );
        ContextAssemblyService service = new ContextAssemblyService(List.of(provider), (item, query) -> !item.sourceId().equals("denied"));

        ContextAssemblyService.ContextResult result = service.retrieve(new ContextQuery(new com.shiyu.ai.kernel.context.TenantId(1), new com.shiyu.ai.kernel.context.UserId(2), "test", "query", 2, Map.of()));

        assertEquals(List.of("allowed", "second"), result.items().stream().map(ContextItem::sourceId).toList());
        assertEquals(2, result.trace().itemIds().size());
    }
}
