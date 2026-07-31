package com.shiyu.ai.knowledge.model;

import com.shiyu.ai.knowledge.rag.RagOrchestrator.RagChunk;
import com.shiyu.ai.knowledge.rag.Reranker;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapts the existing cloud/model-platform reranker to the provider SPI.
 */
@Component
public class ExistingRerankProvider implements RerankProvider {

    private final Reranker reranker;

    public ExistingRerankProvider(Reranker reranker) {
        this.reranker = reranker;
    }

    @Override
    public String profile() {
        return "platform";
    }

    @Override
    public List<Integer> rerank(String query, List<String> candidates, int topK) {
        List<RagChunk> chunks = new ArrayList<>(candidates.size());
        for (int i = 0; i < candidates.size(); i++) {
            chunks.add(new RagChunk(candidates.get(i), 0D, Map.of("_candidateIndex", i)));
        }
        return reranker.rerank(query, chunks, topK).stream()
                .map(chunk -> (Integer) chunk.metadata().get("_candidateIndex"))
                .toList();
    }
}
