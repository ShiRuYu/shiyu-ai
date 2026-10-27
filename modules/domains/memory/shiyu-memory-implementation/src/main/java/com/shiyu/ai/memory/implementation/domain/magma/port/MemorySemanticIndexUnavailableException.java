package com.shiyu.ai.memory.implementation.domain.magma.port;

/**
 * 标识语义记忆操作无法安全使用配置的向量索引。
 */
public class MemorySemanticIndexUnavailableException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public MemorySemanticIndexUnavailableException(String message) {
        super(message);
    }
}
