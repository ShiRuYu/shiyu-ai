package com.shiyu.ai.memory.contract.api;

import com.shiyu.ai.memory.contract.model.IngestMemoryCommand;
import com.shiyu.ai.memory.contract.model.MemoryEvent;

/**
 * MemoryIngestionPort 边界接口，负责向外部组件提供记忆领域相关能力。
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
