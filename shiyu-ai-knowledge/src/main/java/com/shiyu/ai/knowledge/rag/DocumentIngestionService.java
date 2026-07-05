package com.shiyu.ai.knowledge.rag;

import com.shiyu.ai.core.embedding.EmbeddingService;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeChunkDO;
import com.shiyu.ai.knowledge.rag.ChunkSplitter.Chunk;
import com.shiyu.ai.knowledge.rag.impl.ChineseChunkSplitter;
import com.shiyu.ai.knowledge.repository.KnowledgeChunkRepository;
import com.shiyu.ai.knowledge.vector.VectorRecord;
import com.shiyu.ai.knowledge.vector.VectorStore;
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

    public DocumentIngestionService(EmbeddingService embeddingService,
                                    VectorStore vectorStore,
                                    KnowledgeChunkRepository chunkRepository) {
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
        this.chunkRepository = chunkRepository;
        this.chunkSplitter = new ChineseChunkSplitter();
    }
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
