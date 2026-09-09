package com.shiyu.ai.memory.contract.api;

import com.shiyu.ai.memory.contract.model.MemoryPath;
import com.shiyu.ai.memory.contract.model.MemoryQuery;

import java.util.List;

public interface MemoryQueryPort { List<MemoryPath> retrieve(MemoryQuery query); }

