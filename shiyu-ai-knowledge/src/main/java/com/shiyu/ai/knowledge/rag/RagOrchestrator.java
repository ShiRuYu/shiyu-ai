package com.shiyu.ai.knowledge.rag;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.core.embedding.EmbeddingService;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeChunkDO;
import com.shiyu.ai.knowledge.graph.GraphStore;
import com.shiyu.ai.knowledge.repository.KnowledgeChunkRepository;
import com.shiyu.ai.knowledge.vector.VectorRecord;
import com.shiyu.ai.knowledge.vector.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RagOrchestrator {

    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;
    private final KnowledgeChunkRepository chunkRepository;
    private final GraphStore graphStore;

    public RagOrchestrator(EmbeddingService embeddingService,
                           VectorStore vectorStore,
                           KnowledgeChunkRepository chunkRepository,
                           GraphStore graphStore) {
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
        this.chunkRepository = chunkRepository;
        this.graphStore = graphStore;
    }

    public RagResult retrieve(String query, int topK) {
        float[] queryVector = embeddingService.embed(query);
        List<VectorRecord> vsResults = vectorStore.search(queryVector, topK);

        List<RagChunk> chunks = new ArrayList<>();
        Set<String> relatedKnowledgeIds = new LinkedHashSet<>();

        for (VectorRecord r : vsResults) {
            KnowledgeChunkDO chunkDO = chunkRepository.getById(parseChunkId(r.id()));
            if (chunkDO == null) continue;

            double score = (double) r.metadata().getOrDefault("_score", 0.0);

            Map<String, Object> meta = chunkDO.getMetadata() != null
                    ? JSONUtils.parseObject(chunkDO.getMetadata(), Map.class)
                    : new HashMap<>();
            meta.put("_score", score);

            String knowledgeId = (String) meta.get("knowledgeId");
            if (knowledgeId != null) {
                relatedKnowledgeIds.add(knowledgeId);
            }

            chunks.add(new RagChunk(chunkDO.getContent(), score, meta));
        }

        String graphContext = enrichWithGraph(relatedKnowledgeIds);

        return new RagResult(chunks, graphContext);
    }

    private String enrichWithGraph(Set<String> knowledgeIds) {
        if (knowledgeIds.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("\n[知识图谱上下文]\n");

        for (String kid : knowledgeIds) {
            try {
                Long id = Long.parseLong(kid);
                List<Long> parents = graphStore.parents(id);
                List<Long> children = graphStore.children(id);
                List<Long> related = graphStore.related(id);

                if (!parents.isEmpty()) {
                    sb.append("前置知识: ");
                    sb.append(parents.stream()
                            .map(pid -> {
                                var pn = graphStore.getNode(pid);
                                return pn != null ? pn.getName() : String.valueOf(pid);
                            })
                            .collect(Collectors.joining(" → ")));
                    sb.append(" → 当前\n");
                }
                if (!children.isEmpty()) {
                    sb.append("后续知识: ");
                    sb.append("当前 → ");
                    sb.append(children.stream()
                            .map(cid -> {
                                var cn = graphStore.getNode(cid);
                                return cn != null ? cn.getName() : String.valueOf(cid);
                            })
                            .collect(Collectors.joining(" → ")));
                    sb.append("\n");
                }
                if (!related.isEmpty()) {
                    sb.append("相关知识: ");
                    sb.append(related.stream()
                            .map(rid -> {
                                var rn = graphStore.getNode(rid);
                                return rn != null ? rn.getName() : String.valueOf(rid);
                            })
                            .collect(Collectors.joining(", ")));
                    sb.append("\n");
                }
            } catch (Exception e) {
                log.debug("图谱 enrich 跳过 knowledgeId={}: {}", kid, e.getMessage());
            }
        }
        return sb.toString();
    }

    private Long parseChunkId(String id) {
        try {
            int underscore = id.indexOf('_');
            if (underscore > 0) {
                return Long.parseLong(id.substring(0, underscore));
            }
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public record RagChunk(String content, double score, Map<String, Object> metadata) {
    }

    public record RagResult(List<RagChunk> chunks, String graphContext) {
    }
}
