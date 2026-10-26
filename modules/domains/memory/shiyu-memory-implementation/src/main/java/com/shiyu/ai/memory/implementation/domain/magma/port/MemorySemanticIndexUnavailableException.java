package com.shiyu.ai.memory.implementation.domain.magma.port;

/**
 * Signals that semantic memory operations cannot safely use the configured vector index.
 */
public class MemorySemanticIndexUnavailableException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public MemorySemanticIndexUnavailableException(String message) {
        super(message);
    }
}
