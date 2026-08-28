package com.shiyu.ai.memory.magma;
public interface MemoryEventContributor<T> { IngestMemoryCommand contribute(T source); }
