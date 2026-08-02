package com.shiyu.ai.knowledge.index;

import com.shiyu.ai.dal.knowledge.bo.KnowledgeChunkBO;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeDocumentBO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeSpaceDO;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeChunkRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.model.EmbeddingProvider;
import com.shiyu.ai.knowledge.model.RerankProvider;
import com.shiyu.ai.vector.VectorStoreProvider;
import com.shiyu.ai.vector.config.VectorStoreProperties;
import com.shiyu.ai.vector.factory.ConfiguredVectorStoreProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmbeddedIndexRegistryIntegrationTest {

    @TempDir
    Path tempDir;

    private EmbeddedIndexRegistry registry;

    @AfterEach
    void closeRegistry() {
        if (registry != null) registry.closeAll();
    }

    @Test
    void shouldBuildAndSearchVersionedIndexThroughVectorProvider() {
        KnowledgeEnterpriseRepository enterpriseRepository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeDocumentRepository documentRepository = mock(KnowledgeDocumentRepository.class);
        KnowledgeChunkRepository chunkRepository = mock(KnowledgeChunkRepository.class);

        KnowledgeSpaceDO space = new KnowledgeSpaceDO();
        space.setId(10L);
        space.setTenantId(1L);
        space.setActiveIndexVersion(0L);

        KnowledgeDocumentBO document = new KnowledgeDocumentBO();
        document.setId(20L);
        document.setSpaceId(10L);
        document.setTitle("统一向量接口");
        document.setLifecycleStatus("PUBLISHED");

        KnowledgeChunkBO chunk = new KnowledgeChunkBO();
        chunk.setId(30L);
        chunk.setTenantId(1L);
        chunk.setSpaceId(10L);
        chunk.setDocumentId(20L);
        chunk.setContent("Knowledge 通过 vector 模块的公共 Provider 构建和检索索引");
        chunk.setEmbeddingDimension(3);
        chunk.setEmbeddingBinary(toBytes(new float[]{1F, 0F, 0F}));

        when(enterpriseRepository.findSpaceByTenant(1L, 10L)).thenReturn(space);
        when(documentRepository.findBySpace(10L)).thenReturn(List.of(document));
        when(chunkRepository.findBySpace(10L)).thenReturn(List.of(chunk));

        EmbeddingProvider embeddingProvider = new EmbeddingProvider() {
            @Override public String profile() { return "test"; }
            @Override public float[] embed(String text) { return new float[]{1F, 0F, 0F}; }
        };
        RerankProvider rerankProvider = mock(RerankProvider.class);
        VectorStoreProperties vectorProperties = new VectorStoreProperties();
        vectorProperties.setType("jvector");
        vectorProperties.setDimension(3);
        vectorProperties.setDataDir(tempDir.resolve("vector-default").toString());
        VectorStoreProvider provider = new ConfiguredVectorStoreProvider(vectorProperties);

        registry = new EmbeddedIndexRegistry(enterpriseRepository, documentRepository, chunkRepository,
                embeddingProvider, rerankProvider, provider, tempDir.toString(), 5, 60, 1);

        long version = registry.rebuild(1L, 10L);
        List<KnowledgeIndexService.HybridHit> hits = registry.hybridSearch(
                1L, 10L, "统一向量接口", "HYBRID", 5, 0D, false);

        assertThat(version).isEqualTo(1L);
        assertThat(space.getActiveIndexVersion()).isEqualTo(1L);
        assertThat(hits).hasSize(1);
        assertThat(hits.getFirst().chunkId()).isEqualTo(30L);
        assertThat(hits.getFirst().vectorScore()).isGreaterThan(0D);
        assertThat(hits.getFirst().bm25Score()).isGreaterThan(0D);
        verify(enterpriseRepository).updateSpace(any(KnowledgeSpaceDO.class));
    }

    private byte[] toBytes(float[] vector) {
        ByteBuffer buffer = ByteBuffer.allocate(vector.length * Float.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (float value : vector) buffer.putFloat(value);
        return buffer.array();
    }
}
