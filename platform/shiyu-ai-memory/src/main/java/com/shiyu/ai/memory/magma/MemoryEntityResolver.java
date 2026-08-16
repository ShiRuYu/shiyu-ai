package com.shiyu.ai.memory.magma;
import java.util.Optional;
public interface MemoryEntityResolver { Optional<MemoryEntity> resolve(long tenantId, String entityType, String externalRef); }
