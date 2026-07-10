package com.shiyu.ai.memory.compressor;

import com.shiyu.ai.memory.spi.Memory;

import java.util.List;

/**
 * 滑动窗口压缩
 * 保留最近 N 条消息
 */
public class SlidingWindowCompressor implements MemoryCompressor {

    private final int windowSize;

    public SlidingWindowCompressor(int windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public List<Memory> compress(List<Memory> memories) {
        if (memories.size() <= windowSize) {
            return memories;
        }
        return memories.subList(memories.size() - windowSize, memories.size());
    }
}
