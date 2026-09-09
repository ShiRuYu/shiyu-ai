package com.shiyu.ai.memory.contract.api;

import com.shiyu.ai.memory.contract.model.IngestMemoryCommand;
import com.shiyu.ai.memory.contract.model.MemoryEvent;

public interface MemoryIngestionPort { MemoryEvent ingest(IngestMemoryCommand command); }

