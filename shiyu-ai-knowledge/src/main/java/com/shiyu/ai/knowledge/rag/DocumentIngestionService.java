package com.shiyu.ai.knowledge.rag;

import com.shiyu.ai.knowledge.document.DocumentParser;
import com.shiyu.ai.model.embedding.EmbeddingService;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeChunkBO;
import com.shiyu.ai.knowledge.rag.ChunkSplitter.Chunk;
import com.shiyu.ai.knowledge.rag.ChineseChunkSplitter;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeChunkRepository;
import com.shiyu.ai.vector.VectorRecord;
import com.shiyu.ai.vector.VectorStore;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Slf4j
@Service
public class DocumentIngestionService {

    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;
    private final KnowledgeChunkRepository chunkRepository;
    private final ChunkSplitter chunkSplitter;
    private final List<DocumentParser> documentParsers;

    public DocumentIngestionService(EmbeddingService embeddingService,
                                    VectorStore vectorStore,
                                    KnowledgeChunkRepository chunkRepository,
                                    List<DocumentParser> documentParsers) {
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
        this.chunkRepository = chunkRepository;
        this.chunkSplitter = new ChineseChunkSplitter();
        this.documentParsers = documentParsers != null ? documentParsers : List.of();
    }

    /**
     * 根据文件格式获取对应的文档解析器
     */
    public Optional<DocumentParser> findParser(String format) {
        return documentParsers.stream()
                .filter(p -> p.getSupportedFormat().equalsIgnoreCase(format))
                .findFirst();
    }

    /**
     * 解析并注入文档
     *
     * @param documentId   文档 ID
     * @param content      文本内容（已解析）
     * @param knowledgeIds 关联知识点 ID
     */
    public List<KnowledgeChunkBO> ingest(Long documentId, String content, List<Long> knowledgeIds) {
        return ingest(null, documentId, null, content, knowledgeIds);
    }

    public List<KnowledgeChunkBO> ingest(Long spaceId, Long documentId, Long versionId,
                                         String content, List<Long> knowledgeIds) {
        return ingest(null, spaceId, documentId, versionId, content, knowledgeIds);
    }

    public List<KnowledgeChunkBO> ingest(Long tenantId, Long spaceId, Long documentId,
                                         Long versionId, String content, List<Long> knowledgeIds) {
        Long effectiveTenantId = tenantId != null ? tenantId : LoginContextHolder.getCurrentTenantId();
        delete(documentId);
        List<Chunk> chunks = chunkSplitter.split(content);
        log.info("文档 {} 切分为 {} 个 Chunk", documentId, chunks.size());

        List<KnowledgeChunkBO> chunkDOs = new ArrayList<>();
        List<VectorRecord> vectorRecords = new ArrayList<>();

        for (Chunk chunk : chunks) {
            float[] vector = embeddingService.embed(chunk.content());

            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("documentId", documentId);
            if (effectiveTenantId != null) {
                meta.put("tenantId", effectiveTenantId);
            }
            if (spaceId != null) {
                meta.put("spaceId", spaceId);
            }
            meta.put("chunkIndex", chunk.index());
            meta.put("startPos", chunk.startPos());
            meta.put("endPos", chunk.endPos());
            if (knowledgeIds != null && !knowledgeIds.isEmpty()) {
                meta.put("knowledgeId", String.valueOf(knowledgeIds.get(0)));
                meta.put("knowledgeIds", knowledgeIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")));
            }
            KnowledgeChunkBO chunkDO = new KnowledgeChunkBO();
            chunkDO.setDocumentId(documentId);
            chunkDO.setTenantId(effectiveTenantId);
            chunkDO.setSpaceId(spaceId);
            chunkDO.setVersionId(versionId);
            chunkDO.setContent(chunk.content());
            chunkDO.setEmbeddingBinary(toBytes(vector));
            chunkDO.setEmbeddingModel("default");
            chunkDO.setEmbeddingDimension(vector.length);
            chunkDO.setMetadata(JSONUtils.toJsonString(meta));
            chunkDO.setChunkIndex(chunk.index());
            chunkDO.setStartOffset(chunk.startPos());
            chunkDO.setEndOffset(chunk.endPos());
            chunkDO.setTokenCount(Math.max(1, chunk.content().length() / 2));
            chunkDO.setStatus(1);
            chunkDO.setDelFlag(0);
            chunkRepository.insert(chunkDO);
            chunkDOs.add(chunkDO);
            vectorRecords.add(new VectorRecord(String.valueOf(chunkDO.getId()), vector, meta));
        }

        vectorStore.upsertBatch(vectorRecords);

        log.info("文档 {} 注入完成: {} chunks → H2 + VectorStore", documentId, chunkDOs.size());
        return chunkDOs;
    }

    public void delete(Long documentId) {
        List<String> vectorIds = chunkRepository.getByDocumentId(documentId).stream()
                .map(chunk -> String.valueOf(chunk.getId()))
                .toList();
        if (!vectorIds.isEmpty()) {
            vectorStore.deleteBatch(vectorIds);
        }
        chunkRepository.deleteByDocumentId(documentId);
    }

    private byte[] toBytes(float[] vector) {
        ByteBuffer buffer = ByteBuffer.allocate(vector.length * Float.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (float value : vector) {
            buffer.putFloat(value);
        }
        return buffer.array();
    }
}
