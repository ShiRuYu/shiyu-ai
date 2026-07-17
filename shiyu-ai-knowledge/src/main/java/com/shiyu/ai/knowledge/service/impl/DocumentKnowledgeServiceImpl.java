package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.model.embedding.EmbeddingService;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeChunkDO;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDocumentDO;
import com.shiyu.ai.knowledge.rag.DocumentIngestionService;
import com.shiyu.ai.dal.repository.knowledge.KnowledgeChunkRepository;
import com.shiyu.ai.dal.repository.knowledge.KnowledgeDocRelationRepository;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDocRelationDO;
import com.shiyu.ai.dal.repository.knowledge.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.service.DocumentKnowledgeService;
import com.shiyu.ai.vector.VectorRecord;
import com.shiyu.ai.vector.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
public class DocumentKnowledgeServiceImpl implements DocumentKnowledgeService {

    /** 文档 chunk ID 格式: {documentId}_{chunkIndex}，例如 "1_0", "2_3" */
    private static final Pattern CHUNK_ID_PATTERN = Pattern.compile("[0-9]+_[0-9]+");

    private final KnowledgeDocumentRepository documentRepository;
    private final DocumentIngestionService ingestionService;
    private final VectorStore vectorStore;
    private final EmbeddingService embeddingService;
        private final KnowledgeChunkRepository chunkRepository;
    private final KnowledgeDocRelationRepository docRelationRepository;

    public DocumentKnowledgeServiceImpl(KnowledgeDocumentRepository documentRepository,
                                        DocumentIngestionService ingestionService,
                                        VectorStore vectorStore,
                                        EmbeddingService embeddingService,
                                        KnowledgeChunkRepository chunkRepository,
                                        KnowledgeDocRelationRepository docRelationRepository) {
        this.documentRepository = documentRepository;
        this.ingestionService = ingestionService;
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
                this.chunkRepository = chunkRepository;
        this.docRelationRepository = docRelationRepository;
    }

    @Override
    public KnowledgeDocumentVO getById(Long id) {
        var doc = documentRepository.selectById(id);
        if (doc == null) return null;
        return toVO(doc, doc.getContent(), List.of());
    }

    @Override
    public List<KnowledgeDocumentVO> search(String query, int topK) {
        if (vectorStore == null || embeddingService == null) {
            log.warn("VectorStore 不可用，降级到 DB LIKE 搜索");
            return dbLikeSearch(query, topK);
        }

        try {
            // 1. Embed query → VectorStore 搜索文档 chunk
            float[] queryVector = embeddingService.embed(query);
            // 多搜一些候选，因为同一个文档可能有多个 chunk 匹配
            List<VectorRecord> candidates = vectorStore.search(queryVector, topK * 3);

            // 2. 过滤出文档 chunk（ID 匹配 documentId_chunkIndex 格式，排除 kp_* 知识点）
            //    按 documentId 分组，每个文档只保留得分最高的 chunk
            //    candidates 已按 score 降序排列，所以第一个遇到的 chunk 就是最高分
            Map<Long, ChunkResult> bestChunks = new LinkedHashMap<>();

            for (VectorRecord vr : candidates) {
                if (!CHUNK_ID_PATTERN.matcher(vr.id()).matches()) continue;

                Long docId = parseDocumentId(vr.id());
                Integer chunkIndex = parseChunkIndex(vr.id());
                if (docId == null || chunkIndex == null) continue;

                // 已为该文档找到最佳 chunk，跳过
                if (bestChunks.containsKey(docId)) continue;

                // 从 H2 拿 chunk 内容
                KnowledgeChunkDO chunkDO = chunkRepository.getByDocumentIdAndIndex(docId, chunkIndex);
                if (chunkDO == null) continue;

                double score = (double) vr.metadata().getOrDefault("_score", 0.0);
                List<Long> knowledgeIds = extractKnowledgeIds(chunkDO.getMetadata());

                bestChunks.put(docId, new ChunkResult(docId, chunkDO.getContent(), score, knowledgeIds));
            }

            // 3. 查询文档元信息并组装结果
            List<KnowledgeDocumentVO> results = new ArrayList<>();
            for (ChunkResult cr : bestChunks.values()) {
                if (results.size() >= topK) break;

                KnowledgeDocumentDO doc = documentRepository.selectById(cr.documentId);
                if (doc == null) continue;

                results.add(toVO(doc, cr.snippet, cr.knowledgeIds));
            }

            return results;

        } catch (Exception e) {
            log.error("文档向量搜索失败，降级到 DB LIKE", e);
            return dbLikeSearch(query, topK);
        }
    }

    /** DB LIKE 降级搜索 */
    private List<KnowledgeDocumentVO> dbLikeSearch(String keyword, int topK) {
        var docs = documentRepository.searchByKeyword(keyword, topK);
        return docs.stream()
                .map(doc -> toVO(doc, doc.getContent(), List.of()))
                .toList();
    }

    @Override
    public List<KnowledgeDocumentVO> searchByKnowledgeId(Long knowledgeId) {
        // 通过 knowledge_doc_relation 表 SQL 查询关联文档
        var relations = docRelationRepository.selectByKnowledgeId(knowledgeId);
        return relations.stream()
                .map(r -> {
                    var doc = documentRepository.selectById(r.getDocId());
                    if (doc == null) return null;
                    return toVO(doc, doc.getContent(), List.of(knowledgeId));
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeDocumentVO create(CreateDocumentRequest request) {
        var doc = new KnowledgeDocumentDO();
        doc.setTitle(request.title());
        doc.setContent(request.content());
        doc.setDocType(request.docType() != null ? request.docType() : "ARTICLE");
        doc.setSource(request.source());
        doc.setCreateTime(LocalDateTime.now());
        documentRepository.insert(doc);

        // 同步到 VectorStore（ChunkSplit + Embed + VectorStore）
                ingestionService.ingest(doc.getId(), doc.getContent(), request.knowledgeIds());

        // Save knowledge-doc relations to knowledge_doc_relation table
        if (request.knowledgeIds() != null && !request.knowledgeIds().isEmpty()) {
            List<KnowledgeDocRelationDO> relations = request.knowledgeIds().stream()
                    .map(kid -> {
                        KnowledgeDocRelationDO r = new KnowledgeDocRelationDO();
                        r.setDocId(doc.getId());
                        r.setKnowledgeId(kid);
                        r.setRelationType("RELATED");
                        r.setCreateTime(java.time.LocalDateTime.now());
                        return r;
                    })
                    .toList();
            docRelationRepository.insertBatch(relations);
        }

        return toVO(doc, doc.getContent(), request.knowledgeIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UpdateDocumentRequest request) {
        var doc = documentRepository.selectById(id);
        if (doc == null) return;
        if (request.title() != null) doc.setTitle(request.title());
        if (request.content() != null) {
            doc.setContent(request.content());
            // 内容变更时重新 ingest
            ingestionService.ingest(doc.getId(), doc.getContent(), request.knowledgeIds());
        }
        if (request.docType() != null) doc.setDocType(request.docType());
        if (request.source() != null) doc.setSource(request.source());
        doc.setUpdateTime(LocalDateTime.now());
        documentRepository.update(doc);
    }

    @Override
    public void delete(Long id) {
        documentRepository.deleteById(id);
    }

    @Override
    public void deleteByKnowledgeId(Long knowledgeId) {
        docRelationRepository.deleteByKnowledgeId(knowledgeId);
        log.info("已解除知识点 {} 与所有文档的关联", knowledgeId);
    }

    // ---------------------------------------------------------------
    // 辅助方法
    // ---------------------------------------------------------------

    private static KnowledgeDocumentVO toVO(KnowledgeDocumentDO doc, String content, List<Long> knowledgeIds) {
        return new KnowledgeDocumentVO(
                doc.getId(),
                doc.getTitle(),
                content,
                doc.getDocType(),
                doc.getSource(),
                knowledgeIds);
    }

    /** 从 chunk metadata JSON 中提取关联的知识点 ID 列表 */
    @SuppressWarnings("unchecked")
    private static List<Long> extractKnowledgeIds(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) return List.of();
        try {
            Map<String, Object> meta = JSONUtils.parseObject(metadataJson, Map.class);
            String ids = (String) meta.get("knowledgeIds");
            if (ids == null || ids.isBlank()) return List.of();
            return Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /** 从 chunk ID (documentId_chunkIndex) 解析 documentId */
    private static Long parseDocumentId(String chunkId) {
        try {
            int underscore = chunkId.indexOf('_');
            if (underscore > 0) {
                return Long.parseLong(chunkId.substring(0, underscore));
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 从 chunk ID 解析 chunkIndex */
    private static Integer parseChunkIndex(String chunkId) {
        try {
            int underscore = chunkId.indexOf('_');
            if (underscore > 0 && underscore + 1 < chunkId.length()) {
                return Integer.parseInt(chunkId.substring(underscore + 1));
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 内部结构：文档分组后的最佳 chunk 结果 */
    private record ChunkResult(Long documentId, String snippet, double score, List<Long> knowledgeIds) {}
}
