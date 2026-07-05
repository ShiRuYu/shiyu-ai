package com.shiyu.ai.knowledge.rag;

import java.util.List;

public interface ChunkSplitter {

    List<Chunk> split(String text);

    record Chunk(String content, int index, int startPos, int endPos) {
    }
}
