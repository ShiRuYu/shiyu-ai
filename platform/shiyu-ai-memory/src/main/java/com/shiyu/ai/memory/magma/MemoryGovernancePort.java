package com.shiyu.ai.memory.magma;

public interface MemoryGovernancePort {
    void confirm(long tenantId, String eventId);
    void revoke(long tenantId, String eventId);
    MemoryEvent supersede(long tenantId, String oldEventId, IngestMemoryCommand replacement);
}
