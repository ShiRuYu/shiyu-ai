package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeChunkBO;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeDocRelationBO;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeDocumentBO;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeChunkRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeDocRelationRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.rag.DocumentIngestionService;
import com.shiyu.ai.knowledge.service.DocumentKnowledgeService;
import com.shiyu.ai.model.embedding.EmbeddingService;
import com.shiyu.ai.vector.VectorRecord;
import com.shiyu.ai.vector.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Service
public class DocumentKnowledgeServiceImpl implements DocumentKnowledgeService {

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
        KnowledgeDocumentBO document = documentRepository.selectById(id);
        if (document == null) {
            return null;
        }
        return toVO(document, document.getContent(), getKnowledgeIds(id));
    }

    @Override
    public List<KnowledgeDocumentVO> search(String query, int topK) {
        if (vectorStore == null || embeddingService == null || query == null || query.isBlank()) {
            return dbLikeSearch(query == null ? "" : query, topK);
        }
        try {
            float[] queryVector = embeddingService.embed(query);
            List<VectorRecord> candidates = vectorStore.search(queryVector, topK * 3);
            Map<Long, ChunkResult> bestChunks = new LinkedHashMap<>();

            for (VectorRecord candidate : candidates) {
                if (!CHUNK_ID_PATTERN.matcher(candidate.id()).matches()) {
                    continue;
                }
                Long documentId = parseDocumentId(candidate.id());
                Integer chunkIndex = parseChunkIndex(candidate.id());
                if (documentId == null || chunkIndex == null || bestChunks.containsKey(documentId)) {
                    continue;
                }
                KnowledgeChunkBO chunk = chunkRepository.getByDocumentIdAndIndex(documentId, chunkIndex);
                if (chunk == null) {
                    continue;
                }
                Object scoreValue = candidate.metadata().getOrDefault("_score", 0.0);
                double score = scoreValue instanceof Number number ? number.doubleValue() : 0.0;
                bestChunks.put(documentId,
                        new ChunkResult(documentId, chunk.getContent(), score,
                                extractKnowledgeIds(chunk.getMetadata())));
            }

            List<KnowledgeDocumentVO> results = new ArrayList<>();
            for (ChunkResult chunk : bestChunks.values()) {
                if (results.size() >= topK) {
                    break;
                }
                KnowledgeDocumentBO document = documentRepository.selectById(chunk.documentId());
                if (document != null) {
                    results.add(toVO(document, chunk.snippet(), getKnowledgeIds(document.getId())));
                }
            }
            return results;
        } catch (Exception exception) {
            log.error("Document vector search failed; falling back to database search", exception);
            return dbLikeSearch(query, topK);
        }
    }

    private List<KnowledgeDocumentVO> dbLikeSearch(String keyword, int topK) {
        return documentRepository.searchByKeyword(keyword, topK).stream()
                .map(document -> toVO(document, document.getContent(), getKnowledgeIds(document.getId())))
                .toList();
    }

    @Override
    public List<KnowledgeDocumentVO> searchByKnowledgeId(Long knowledgeId) {
        return docRelationRepository.selectByKnowledgeId(knowledgeId).stream()
                .map(relation -> documentRepository.selectById(relation.getDocId()))
                .filter(Objects::nonNull)
                .map(document -> toVO(document, document.getContent(), getKnowledgeIds(document.getId())))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeDocumentVO create(CreateDocumentRequest request) {
        KnowledgeDocumentBO document = new KnowledgeDocumentBO();
        document.setTitle(request.title());
        document.setContent(request.content());
        document.setDocType(request.docType() != null ? request.docType() : "ARTICLE");
        document.setSource(request.source());
        document.setCreateTime(LocalDateTime.now());
        documentRepository.insert(document);

        List<Long> knowledgeIds = normalizeKnowledgeIds(request.knowledgeIds());
        replaceRelations(document.getId(), knowledgeIds);
        ingestionService.ingest(document.getId(), document.getContent(), knowledgeIds);
        return toVO(document, document.getContent(), knowledgeIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UpdateDocumentRequest request) {
        KnowledgeDocumentBO document = documentRepository.selectById(id);
        if (document == null) {
            return;
        }
        boolean contentChanged = request.content() != null;
        boolean relationsChanged = request.knowledgeIds() != null;
        if (request.title() != null) {
            document.setTitle(request.title());
        }
        if (request.content() != null) {
            document.setContent(request.content());
        }
        if (request.docType() != null) {
            document.setDocType(request.docType());
        }
        if (request.source() != null) {
            document.setSource(request.source());
        }
        document.setUpdateTime(LocalDateTime.now());
        documentRepository.update(document);

        List<Long> knowledgeIds = relationsChanged
                ? normalizeKnowledgeIds(request.knowledgeIds())
                : getKnowledgeIds(id);
        if (relationsChanged) {
            replaceRelations(id, knowledgeIds);
        }
        if (contentChanged || relationsChanged) {
            ingestionService.ingest(id, document.getContent(), knowledgeIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ingestionService.delete(id);
        docRelationRepository.deleteByDocId(id);
        documentRepository.deleteById(id);
    }

    @Override
    public void deleteByKnowledgeId(Long knowledgeId) {
        docRelationRepository.deleteByKnowledgeId(knowledgeId);
        log.info("Removed all document relations for knowledge point {}", knowledgeId);
    }

    private List<Long> getKnowledgeIds(Long documentId) {
        return docRelationRepository.selectByDocId(documentId).stream()
                .map(KnowledgeDocRelationBO::getKnowledgeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private void replaceRelations(Long documentId, List<Long> knowledgeIds) {
        docRelationRepository.deleteByDocId(documentId);
        if (knowledgeIds.isEmpty()) {
            return;
        }
        List<KnowledgeDocRelationBO> relations = knowledgeIds.stream()
                .map(knowledgeId -> {
                    KnowledgeDocRelationBO relation = new KnowledgeDocRelationBO();
                    relation.setDocId(documentId);
                    relation.setKnowledgeId(knowledgeId);
                    relation.setRelationType("RELATED");
                    relation.setCreateTime(LocalDateTime.now());
                    return relation;
                })
                .toList();
        docRelationRepository.insertBatch(relations);
    }

    private static List<Long> normalizeKnowledgeIds(List<Long> knowledgeIds) {
        if (knowledgeIds == null || knowledgeIds.isEmpty()) {
            return List.of();
        }
        return knowledgeIds.stream().filter(Objects::nonNull).distinct().toList();
    }

    private static KnowledgeDocumentVO toVO(KnowledgeDocumentBO document, String content,
                                             List<Long> knowledgeIds) {
        return new KnowledgeDocumentVO(
                document.getId(),
                document.getTitle(),
                content,
                document.getDocType(),
                document.getSource(),
                knowledgeIds);
    }

    @SuppressWarnings("unchecked")
    private static List<Long> extractKnowledgeIds(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return List.of();
        }
        try {
            Map<String, Object> metadata = JSONUtils.parseObject(metadataJson, Map.class);
            String ids = (String) metadata.get("knowledgeIds");
            if (ids == null || ids.isBlank()) {
                return List.of();
            }
            return Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .map(Long::parseLong)
                    .toList();
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private static Long parseDocumentId(String chunkId) {
        try {
            int separator = chunkId.indexOf('_');
            return separator > 0 ? Long.parseLong(chunkId.substring(0, separator)) : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Integer parseChunkIndex(String chunkId) {
        try {
            int separator = chunkId.indexOf('_');
            return separator > 0 && separator + 1 < chunkId.length()
                    ? Integer.parseInt(chunkId.substring(separator + 1))
                    : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private record ChunkResult(Long documentId, String snippet, double score,
                               List<Long> knowledgeIds) {
    }
}
