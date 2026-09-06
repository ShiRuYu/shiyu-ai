package com.shiyu.ai.knowledge.model;

import java.util.List;

public interface RerankProvider {
    String profile();
    List<Integer> rerank(String query, List<String> candidates, int topK);
}
