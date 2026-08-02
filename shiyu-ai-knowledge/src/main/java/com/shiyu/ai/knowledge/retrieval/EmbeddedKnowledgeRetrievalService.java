package com.shiyu.ai.knowledge.retrieval;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeBO;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeChunkBO;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeChunkRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.index.KnowledgeIndexService;
import com.shiyu.ai.knowledge.security.KnowledgeAccessContext;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmbeddedKnowledgeRetrievalService implements KnowledgeRetrievalService {

    private final KnowledgeSpaceService spaceService;
    private final KnowledgeIndexService indexService;
    private final KnowledgeRepository knowledgeRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final KnowledgeDocumentRepository documentRepository;

    @Override
    public KnowledgeRetrievalResult retrieve(KnowledgeRetrievalRequest request) {
        if (request == null || request.accessContext() == null) {
            return KnowledgeRetrievalResult.failure("知识检索缺少安全上下文");
        }
        if (request.query() == null || request.query().isBlank()) {
            return KnowledgeRetrievalResult.failure("检索问题不能为空");
        }

        List<KnowledgeSpaceService.SpaceView> spaces = resolveSpaces(request);
        if (spaces.isEmpty()) {
            return new KnowledgeRetrievalResult(true, List.of(), List.of(), "", null);
        }

        List<KnowledgeRetrievalHit> hits = new ArrayList<>();
        for (KnowledgeSpaceService.SpaceView space : spaces) {
            if (request.sourceTypes().contains(KnowledgeSourceType.DOCUMENT)) {
                hits.addAll(searchDocuments(request, space));
            }
            if (request.sourceTypes().contains(KnowledgeSourceType.KNOWLEDGE_ENTRY)) {
                hits.addAll(searchKnowledgeEntries(request, space));
            }
        }

        Map<String, KnowledgeRetrievalHit> unique = new LinkedHashMap<>();
        hits.stream()
                .sorted(Comparator.comparingDouble(this::rankScore).reversed())
                .forEach(hit -> unique.putIfAbsent(hit.spaceId() + ":" +
                        (hit.chunkId() != null ? "c" + hit.chunkId() : "k" + hit.knowledgeId()), hit));
        List<KnowledgeRetrievalHit> selected = unique.values().stream()
                .limit(Math.max(1, Math.min(100, request.topK())))
                .toList();

        List<KnowledgeCitation> citations = new ArrayList<>();
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < selected.size(); i++) {
            KnowledgeRetrievalHit hit = selected.get(i);
            String citationId = "c" + (i + 1);
            citations.add(new KnowledgeCitation(citationId, hit.spaceId(), hit.knowledgeId(),
                    hit.documentId(), hit.documentVersionId(), hit.chunkId(), hit.title(),
                    hit.pageNumber(), hit.sectionPath(), excerpt(hit.content())));
            context.append("[").append(citationId).append("] ")
                    .append(hit.title() == null ? "" : hit.title()).append("\n")
                    .append(hit.content() == null ? "" : hit.content()).append("\n\n");
        }
        return new KnowledgeRetrievalResult(true, selected, citations, context.toString(), null);
    }

    private List<KnowledgeSpaceService.SpaceView> resolveSpaces(KnowledgeRetrievalRequest request) {
        KnowledgeAccessContext context = request.accessContext();
        if (request.spaceIds() == null || request.spaceIds().isEmpty()) {
            return spaceService.accessibleSpaces(context);
        }
        Map<Long, KnowledgeSpaceService.SpaceView> accessible = spaceService.accessibleSpaces(context).stream()
                .collect(java.util.stream.Collectors.toMap(KnowledgeSpaceService.SpaceView::id, value -> value));
        return request.spaceIds().stream().distinct().map(id -> {
            spaceService.requireAccess(id, KnowledgeSpaceService.SpaceRole.VIEWER, context);
            KnowledgeSpaceService.SpaceView space = accessible.get(id);
            if (space == null) {
                throw new ServiceException("无权访问知识空间: " + id);
            }
            return space;
        }).toList();
    }

    private List<KnowledgeRetrievalHit> searchDocuments(KnowledgeRetrievalRequest request,
                                                         KnowledgeSpaceService.SpaceView space) {
        String mode = request.retrievalMode().name();
        List<KnowledgeIndexService.HybridHit> indexHits = indexService.hybridSearch(
                request.accessContext().tenantId(), space.id(), request.query(), mode,
                Math.max(request.candidateTopK(), request.topK()), request.scoreThreshold(),
                Boolean.TRUE.equals(request.enableRerank()));
        return indexHits.stream().map(hit -> {
            KnowledgeChunkBO chunk = chunkRepository.getById(hit.chunkId());
            return new KnowledgeRetrievalHit(space.id(), knowledgeId(chunk), hit.documentId(),
                    chunk == null ? null : chunk.getVersionId(), hit.chunkId(),
                    documentTitle(hit.documentId()), hit.content(), hit.highlight(),
                    chunk == null ? null : chunk.getPageNumber(),
                    chunk == null ? null : chunk.getSectionPath(), hit.bm25Score(),
                    hit.vectorScore(), hit.rrfScore(), hit.rerankScore());
        }).toList();
    }

    private List<KnowledgeRetrievalHit> searchKnowledgeEntries(KnowledgeRetrievalRequest request,
                                                                KnowledgeSpaceService.SpaceView space) {
        List<KnowledgeBO> entries = knowledgeRepository.findBySpace(space.id()).stream()
                .filter(k -> contains(k.getName(), request.query()) || contains(k.getDescription(), request.query()))
                .limit(Math.max(request.candidateTopK(), request.topK()))
                .toList();
        return entries.stream().map(k -> new KnowledgeRetrievalHit(space.id(), k.getId(), null,
                null, null, k.getName(), k.getDescription() == null ? k.getName() : k.getDescription(),
                null, null, null, 1D, 0D, 1D, 0D)).toList();
    }

    private Long knowledgeId(KnowledgeChunkBO chunk) {
        if (chunk == null || chunk.getMetadata() == null) return null;
        String value = chunk.getMetadata().replaceAll(".*\\\"knowledgeId\\\"\\s*:\\s*\\\"?([0-9]+).*", "$1");
        try { return value.equals(chunk.getMetadata()) ? null : Long.valueOf(value); }
        catch (NumberFormatException ignored) { return null; }
    }

    private String documentTitle(Long documentId) {
        if (documentId == null) return "";
        var document = documentRepository.selectById(documentId);
        return document == null || document.getTitle() == null
                ? "文档 " + documentId : document.getTitle();
    }

    private double rankScore(KnowledgeRetrievalHit hit) {
        if (hit.rerankScore() > 0D) return hit.rerankScore();
        if (hit.rrfScore() > 0D) return hit.rrfScore();
        return Math.max(hit.vectorScore(), hit.bm25Score());
    }

    private String excerpt(String content) {
        if (content == null) return "";
        return content.length() <= 300 ? content : content.substring(0, 300);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query.toLowerCase());
    }
}
