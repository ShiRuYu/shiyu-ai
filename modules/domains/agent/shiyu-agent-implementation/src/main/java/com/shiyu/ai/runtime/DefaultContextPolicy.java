package com.shiyu.ai.runtime;

import org.springframework.stereotype.Component;

@Component
public class DefaultContextPolicy implements ContextPolicy {
    @Override public boolean canRead(ContextItem item, ContextQuery query) {
        return item != null && query != null && item.accessScope() != null && !item.accessScope().isBlank();
    }
}
