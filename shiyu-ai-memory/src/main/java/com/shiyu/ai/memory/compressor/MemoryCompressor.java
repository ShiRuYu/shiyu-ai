package com.shiyu.ai.memory.compressor;

import com.shiyu.ai.memory.spi.Memory;

import java.util.List;

/**
 * 记忆压缩接口
 */
public interface MemoryCompressor {

    List<Memory> compress(List<Memory> memories);
}
