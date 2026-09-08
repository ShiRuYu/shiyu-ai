package com.shiyu.ai.runtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Single assembly boundary for Knowledge, MAGMA and future context providers.
 * Providers remain domain-owned; callers only receive the common contract.
 */
@Service
public class ContextAssemblyService implements ContextAssemblyPort {
    private final List<ContextRetrievalPort> providers;
    private final ContextPolicy policy;

    @Autowired
    public ContextAssemblyService(List<ContextRetrievalPort> providers, ContextPolicy policy) {
        this.providers = providers == null ? List.of() : List.copyOf(providers);
        this.policy = policy == null ? new DefaultContextPolicy() : policy;
    }

    public ContextAssemblyPort.ContextResult retrieve(ContextQuery query) {
        List<ContextItem> items = providers.stream()
                .flatMap(provider -> provider.retrieve(query).stream())
                .filter(item -> policy.canRead(item, query))
                .sorted(Comparator.comparingDouble(ContextItem::score).reversed())
                .limit(query.topK())
                .toList();
        return new ContextAssemblyPort.ContextResult(items, new ContextTrace(UUID.randomUUID().toString(), query.tenantId(), query.text(),
                items.stream().map(ContextItem::sourceId).toList(), query.namespace(), Instant.now()));
    }

}
