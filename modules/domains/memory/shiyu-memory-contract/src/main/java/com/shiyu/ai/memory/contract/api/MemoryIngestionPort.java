package com.shiyu.ai.memory.contract.api;

import com.shiyu.ai.memory.contract.model.IngestMemoryCommand;
import com.shiyu.ai.memory.contract.model.MemoryEvent;

/**
 * 定义 记忆 Ingestion 领域与外部能力交互的端口契约。
 */
public interface MemoryIngestionPort {
    /**
     * 处理ingest。
     *
     * @param command command 参数。
     *
     * @return 处理结果。
     */
    MemoryEvent ingest(IngestMemoryCommand command);
}
