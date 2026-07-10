package com.shiyu.ai.model.embedding;

import java.util.List;

public interface EmbeddingService {

    float[] embed(String text);

    List<float[]> embedBatch(List<String> texts);

    int dimension();
}
