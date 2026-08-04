package com.shiyu.ai.memory.spi.impl;

import com.shiyu.ai.memory.spi.Memory;
import com.shiyu.ai.memory.spi.MemoryQuery;
import com.shiyu.ai.memory.spi.MemoryType;
import com.shiyu.ai.model.embedding.EmbeddingService;
import com.shiyu.ai.vector.VectorStore;
import com.shiyu.ai.vector.VectorStoreOptions;
import com.shiyu.ai.vector.VectorStoreProvider;
import com.shiyu.ai.vector.config.VectorStoreProperties;
import com.shiyu.ai.vector.factory.ConfiguredVectorStoreProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticMemoryStoreIntegrationTest {

    @Test
    void shouldSaveQueryAndDeleteThroughVectorModuleInterface() {
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setType("inmemory");
        properties.setDimension(3);
        VectorStoreProvider provider = new ConfiguredVectorStoreProvider(properties);
        VectorStore vectorStore = provider.open(VectorStoreOptions.of("semantic-memory", 3, null));
        SemanticMemoryStore store = new SemanticMemoryStore(vectorStore, new FixedEmbeddingService());

        Memory memory = new Memory(MemoryType.SEMANTIC, "session-1", "assistant", "统一向量接口");
        memory.setUserId(7L);
        memory.setAgentId("agent-1");
        store.save(memory);

        List<Memory> found = store.query(MemoryQuery.builder()
                .keyword("统一向量接口")
                .userId(7L)
                .agentId("agent-1")
                .topK(5)
                .build());

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getMemoryId()).isEqualTo(memory.getMemoryId());
        assertThat(found.getFirst().getContent()).isEqualTo("统一向量接口");

        store.delete(memory.getMemoryId());
        assertThat(store.query(MemoryQuery.builder()
                .keyword("统一向量接口").userId(7L).agentId("agent-1").build())).isEmpty();
        provider.close();
    }

    private static final class FixedEmbeddingService implements EmbeddingService {
        @Override
        public float[] embed(String text) {
            return text.contains("统一") ? new float[]{1F, 0F, 0F} : new float[]{0F, 1F, 0F};
        }

        @Override
        public List<float[]> embedBatch(List<String> texts) {
            return texts.stream().map(this::embed).toList();
        }

        @Override
        public int dimension() {
            return 3;
        }
    }
}
