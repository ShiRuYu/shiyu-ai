package com.shiyu.ai.memory.magma;
public interface MemoryAccessPolicy { boolean canRead(long tenantId, String namespace, String subjectType, String subjectId, String sourceType, String sourceId); }
