package com.shiyu.ai.memory.magma;

import java.util.List;

public interface MemoryQueryPort { List<MemoryPath> retrieve(MemoryQuery query); }
