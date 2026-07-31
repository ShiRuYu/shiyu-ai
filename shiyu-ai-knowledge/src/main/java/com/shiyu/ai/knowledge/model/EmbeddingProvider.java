package com.shiyu.ai.knowledge.model;

public interface EmbeddingProvider {
    String profile();
    float[] embed(String text);
}
