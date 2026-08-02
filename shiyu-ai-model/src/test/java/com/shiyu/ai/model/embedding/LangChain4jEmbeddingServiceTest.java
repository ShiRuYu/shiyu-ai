package com.shiyu.ai.model.embedding;

import com.shiyu.ai.model.embedding.impl.LangChain4jEmbeddingService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class LangChain4jEmbeddingServiceTest {

    @Test
    void localModelIsOptionalInTheBasePackage() {
        LangChain4jEmbeddingService service = new LangChain4jEmbeddingService();

        assertTrue(service.dimension() >= 0);
        if (localModelAvailable()) {
            assertTrue(service.dimension() > 0);
        } else {
            assertEquals(0, service.dimension());
        }
    }

    @Test
    void offlineBgeModelProducesVectorsWhenTheOfflineProfileIsEnabled() {
        assumeTrue(localModelAvailable(), "offline-models profile is not enabled");

        LangChain4jEmbeddingService service = new LangChain4jEmbeddingService();
        float[] vector = service.embed("企业知识平台离线模型验证");

        assertNotNull(vector);
        assertEquals(512, vector.length);
    }

    private boolean localModelAvailable() {
        try {
            Class.forName("dev.langchain4j.model.embedding.onnx.bgesmallzhv15.BgeSmallZhV15EmbeddingModel");
            return true;
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }
}
