package com.shiyu.ai.knowledge.rag;

import com.shiyu.ai.knowledge.rag.RagOrchestrator.RagChunk;

import java.util.List;

/**
 * 重排序 SPI
 */
public interface Reranker {

    List<RagChunk> rerank(String query, List<RagChunk> chunks, int topK);
}
