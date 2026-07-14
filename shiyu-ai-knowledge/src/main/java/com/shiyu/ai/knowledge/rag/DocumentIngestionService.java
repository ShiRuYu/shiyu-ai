package com.shiyu.ai.knowledge.rag;

import com.shiyu.ai.knowledge.document.DocumentParser;
import com.shiyu.ai.model.embedding.EmbeddingService;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeChunkDO;
import com.shiyu.ai.knowledge.rag.ChunkSplitter.Chunk;
import com.shiyu.ai.knowledge.rag.ChineseChunkSplitter;
import com.shiyu.ai.dal.repository.knowledge.KnowledgeChunkRepository;
import com.shiyu.ai.vector.spi.VectorRecord;
import com.shiyu.ai.vector.spi.VectorStore;
import com.shiyu.ai.common.core.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

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
    public List<KnowledgeChunkDO> ingest(Long documentId, String content, List<Long> knowledgeIds) {
        List<Chunk> chunks = chunkSplitter.split(content);
        log.info("文档 {} 切分为 {} 个 Chunk", documentId, chunks.size());

        List<KnowledgeChunkDO> chunkDOs = new ArrayList<>();
        List<VectorRecord> vectorRecords = new ArrayList<>();

        for (Chunk chunk : chunks) {
            float[] vector = embeddingService.embed(chunk.content());

            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("documentId", documentId);
            meta.put("chunkIndex", chunk.index());
            meta.put("startPos", chunk.startPos());
            meta.put("endPos", chunk.endPos());
            if (knowledgeIds != null && !knowledgeIds.isEmpty()) {
                meta.put("knowledgeId", String.valueOf(knowledgeIds.get(0)));
                meta.put("knowledgeIds", knowledgeIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")));
            }
            String id = documentId + "_" + chunk.index();

            vectorRecords.add(new VectorRecord(id, vector, meta));

            KnowledgeChunkDO chunkDO = new KnowledgeChunkDO();
            chunkDO.setDocumentId(documentId);
            chunkDO.setContent(chunk.content());
            chunkDO.setEmbedding(JSONUtils.toJsonString(vector));
            chunkDO.setMetadata(JSONUtils.toJsonString(meta));
            chunkDO.setChunkIndex(chunk.index());
            chunkDOs.add(chunkDO);
        }

        chunkRepository.deleteByDocumentId(documentId);
        chunkRepository.insertBatch(chunkDOs);
        vectorStore.upsertBatch(vectorRecords);

        log.info("文档 {} 注入完成: {} chunks → H2 + VectorStore", documentId, chunkDOs.size());
        return chunkDOs;
    }
}
