package com.shiyu.ai.memory.contract.api;

import com.shiyu.ai.memory.contract.model.IngestMemoryCommand;
import com.shiyu.ai.memory.contract.model.MemoryEvent;

/** Inbound boundary for converting an explicit command into a durable memory event. */
public interface MemoryIngestionPort {
    /** Ingests one event and returns the persisted canonical representation. */
    MemoryEvent ingest(IngestMemoryCommand command);
}
