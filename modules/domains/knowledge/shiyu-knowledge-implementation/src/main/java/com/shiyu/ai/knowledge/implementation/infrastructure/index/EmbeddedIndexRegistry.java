package com.shiyu.ai.knowledge.implementation.infrastructure.index;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.vector.api.VectorStoreProvider;
import com.shiyu.ai.knowledge.implementation.domain.model.EmbeddingProvider;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeChunkBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.implementation.domain.model.RerankProvider;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeChunkRepository;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Coordinates tenant-scoped search while {@link EmbeddedIndexStorage} owns index resources. */
@Slf4j
@Service
@SuppressWarnings("deprecation")
public class EmbeddedIndexRegistry implements KnowledgeIndexService {
    private final KnowledgeEnterpriseRepository enterpriseRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final EmbeddingProvider embeddingProvider;
    private final RerankProvider rerankProvider;
    private final EmbeddedIndexStorage storage;
    private final int rrfK;

    public EmbeddedIndexRegistry(KnowledgeEnterpriseRepository enterpriseRepository,
                                 KnowledgeDocumentRepository documentRepository,
                                 KnowledgeChunkRepository chunkRepository,
                                 EmbeddingProvider embeddingProvider,
                                 RerankProvider rerankProvider,
                                 VectorStoreProvider vectorStoreProvider,
                                 @Value("${shiyu.knowledge.data-dir:${app.home}/data}") String dataDir,
                                 @Value("${shiyu.knowledge.index.idle-minutes:15}") long idleMinutes,
                                 @Value("${shiyu.knowledge.index.rrf-k:60}") int rrfK,
                                 @Value("${shiyu.knowledge.index.rollback-versions:1}") int rollbackVersions) {
        this.enterpriseRepository = enterpriseRepository;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingProvider = embeddingProvider;
        this.rerankProvider = rerankProvider;
        this.storage = new EmbeddedIndexStorage(vectorStoreProvider, dataDir, idleMinutes, rollbackVersions);
        this.rrfK = rrfK;
    }

    @Override
    public synchronized long rebuild(TenantId tenant, Long spaceId) {
        long tenantId = requireTenant(tenant);
        KnowledgeSpaceBO space = enterpriseRepository.findSpaceByTenant(tenant, spaceId);
        if (space == null) throw new ServiceException("知识空间不存在: " + spaceId);
        if (!Long.valueOf(tenantId).equals(space.getTenantId())) throw new ServiceException("租户与知识空间不匹配");
        long previousVersion = space.getActiveIndexVersion() == null ? 0 : space.getActiveIndexVersion();
        long version = previousVersion + 1;
        Path versionPath = storage.path(tenantId, spaceId, version);
        try {
            Files.createDirectories(versionPath);
            List<KnowledgeDocumentBO> published = documentRepository.findBySpace(tenant, spaceId).stream()
                    .filter(document -> "PUBLISHED".equals(document.getLifecycleStatus())).toList();
            Map<Long, KnowledgeDocumentBO> documents = published.stream()
                    .collect(Collectors.toMap(KnowledgeDocumentBO::getId, Function.identity()));
            List<KnowledgeChunkBO> chunks = chunkRepository.findBySpace(tenant, spaceId).stream()
                    .filter(chunk -> documents.containsKey(chunk.getDocumentId())).toList();
            storage.buildLucene(versionPath.resolve("lucene"), chunks, documents);
            storage.buildVector(versionPath.resolve("vector"), tenantId, spaceId, version, chunks);
            space.setActiveIndexVersion(version);
            enterpriseRepository.updateSpace(tenant, space);
            storage.invalidate(new IndexKey(tenantId, spaceId, version));
            storage.cleanupOldVersions(tenantId, spaceId, version);
            log.info("Activated embedded index tenantSelected={}, spacePresent={}, versionPresent={}, chunks={}",
                    tenantId > 0, spaceId != null, version > 0, chunks.size());
            return version;
        } catch (Exception exception) {
            storage.deleteTree(versionPath);
            log.error("构建嵌入式索引失败: tenantSelected={}, spacePresent={}, versionPresent={}, errorType={}, errorMessageLength={}",
                    tenantId > 0, spaceId != null, version > 0, exception.getClass().getSimpleName(),
                    exception.getMessage() == null ? 0 : exception.getMessage().length());
            throw new ServiceException("构建嵌入式索引失败");
        }
    }

    @Override
    public List<FullTextHit> search(TenantId tenantId, Long spaceId, Long version, String queryText, int topK) {
        EmbeddedIndexStorage.IndexHandle handle = storage.handle(new IndexKey(requireTenant(tenantId), spaceId, version));
        try {
            Query query = new MultiFieldQueryParser(new String[]{"title", "content"}, handle.analyzer())
                    .parse(MultiFieldQueryParser.escape(queryText));
            TopDocs topDocs = handle.searcher().search(query, topK);
            UnifiedHighlighter highlighter = new UnifiedHighlighter(handle.searcher(), handle.analyzer());
            String[] highlights = highlighter.highlight("content", query, topDocs, 1);
            List<FullTextHit> hits = new ArrayList<>();
            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                var scoreDoc = topDocs.scoreDocs[i];
                Document document = handle.searcher().storedFields().document(scoreDoc.doc);
                hits.add(new FullTextHit(Long.parseLong(document.get("chunkId")),
                        Long.parseLong(document.get("documentId")), scoreDoc.score,
                        i < highlights.length ? highlights[i] : document.get("content")));
            }
            return hits;
        } catch (Exception exception) {
            log.error("全文检索失败: tenantSelected={}, spacePresent={}, versionPresent={}, errorType={}, errorMessageLength={}",
                    tenantId != null, spaceId != null, version != null, exception.getClass().getSimpleName(),
                    exception.getMessage() == null ? 0 : exception.getMessage().length());
            throw new ServiceException("全文检索失败，请稍后重试");
        }
    }

    @Override
    public List<VectorHit> search(TenantId tenantId, Long spaceId, Long version, float[] queryVector, int topK) {
        return storage.handle(new IndexKey(requireTenant(tenantId), spaceId, version)).vectorStore().search(queryVector, topK)
                .stream().map(record -> new VectorHit(Long.parseLong(record.id()),
                        ((Number) record.metadata().getOrDefault("_score", 0D)).doubleValue())).toList();
    }

    @Override
    public List<HybridHit> hybridSearch(TenantId tenantId, Long spaceId, String query,
                                        String mode, int topK, double threshold, boolean rerank) {
        requireTenant(tenantId);
        return hybridSearchInternal(null, tenantId, spaceId, query, mode, topK, threshold, rerank);
    }

    @Override
    public List<HybridHit> hybridSearch(ActorContext actor, Long spaceId, String query,
                                        String mode, int topK, double threshold, boolean rerank) {
        if (actor == null) throw new IllegalArgumentException("actor is required");
        return hybridSearchInternal(actor, actor.tenantId(), spaceId, query, mode, topK, threshold, rerank);
    }

    private List<HybridHit> hybridSearchInternal(ActorContext actor, TenantId tenantId, Long spaceId, String query,
                                                  String mode, int topK, double threshold, boolean rerank) {
        requireTenant(tenantId);
        KnowledgeSpaceBO space = enterpriseRepository.findSpaceByTenant(tenantId, spaceId);
        if (space == null || space.getActiveIndexVersion() == null || space.getActiveIndexVersion() <= 0) return List.of();
        String normalizedMode = mode == null || mode.isBlank() ? "HYBRID" : mode.trim().toUpperCase(java.util.Locale.ROOT);
        if (!Set.of("KEYWORD", "SEMANTIC", "VECTOR", "HYBRID").contains(normalizedMode)) {
            throw new ServiceException("不支持的检索模式: " + mode);
        }
        int requestedTopK = Math.max(1, Math.min(100, topK));
        double minScore = Math.max(0D, threshold);
        int candidates = Math.max(20, requestedTopK);
        List<FullTextHit> textHits = search(tenantId, spaceId, space.getActiveIndexVersion(), query, candidates);
        if ("KEYWORD".equals(normalizedMode)) return keywordHits(tenantId, textHits, requestedTopK, minScore);
        List<VectorHit> vectorHits;
        try {
            float[] queryVector = actor == null ? embeddingProvider.embed(tenantId, query) : embeddingProvider.embed(actor, query);
            vectorHits = search(tenantId, spaceId, space.getActiveIndexVersion(), queryVector, candidates);
        } catch (IllegalStateException exception) {
            if ("HYBRID".equals(normalizedMode)) {
                log.warn("向量检索未启用，HYBRID 降级为全文检索: errorType={}, errorMessageLength={}",
                        exception.getClass().getSimpleName(), exception.getMessage() == null ? 0 : exception.getMessage().length());
                return keywordHits(tenantId, textHits, requestedTopK, minScore);
            }
            throw new ServiceException("向量检索未启用，请配置 Embedding 模型或使用 KEYWORD/HYBRID 模式");
        }
        if ("SEMANTIC".equals(normalizedMode) || "VECTOR".equals(normalizedMode)) {
            return vectorHits.stream().filter(hit -> minScore <= 0D || hit.score() >= minScore).limit(requestedTopK)
                    .map(hit -> vectorHit(tenantId, hit, null)).toList();
        }
        Map<Long, MutableHit> merged = new LinkedHashMap<>();
        for (int i = 0; i < textHits.size(); i++) {
            FullTextHit hit = textHits.get(i);
            MutableHit item = merged.computeIfAbsent(hit.chunkId(), MutableHit::new);
            item.documentId = hit.documentId(); item.highlight = hit.highlight(); item.bm25 = hit.score(); item.rrf += 1D / (rrfK + i + 1);
        }
        for (int i = 0; i < vectorHits.size(); i++) {
            VectorHit hit = vectorHits.get(i);
            MutableHit item = merged.computeIfAbsent(hit.chunkId(), MutableHit::new);
            item.vector = hit.score(); item.rrf += 1D / (rrfK + i + 1);
        }
        Map<Long, KnowledgeChunkBO> chunks = chunkRepository.findBySpace(tenantId, spaceId).stream()
                .collect(Collectors.toMap(KnowledgeChunkBO::getId, Function.identity()));
        List<MutableHit> ranked = new ArrayList<>(merged.values());
        ranked.sort(Comparator.comparingDouble((MutableHit item) -> item.rrf).reversed());
        if (rerank && !ranked.isEmpty()) {
            List<MutableHit> rrfRanked = ranked;
            List<String> contents = ranked.stream().map(item -> {
                KnowledgeChunkBO chunk = chunks.get(item.chunkId); return chunk == null ? "" : chunk.getContent();
            }).toList();
            List<Integer> order = rerankProvider.rerank(query, contents, Math.min(topK, ranked.size()));
            Map<Integer, Integer> rerankPositions = new HashMap<>();
            for (int i = 0; i < order.size(); i++) {
                Integer candidateIndex = order.get(i);
                if (candidateIndex != null && candidateIndex >= 0 && candidateIndex < ranked.size()) {
                    rerankPositions.putIfAbsent(candidateIndex, i);
                    ranked.get(candidateIndex).rerank = 1D - (double) i / Math.max(1, order.size());
                }
            }
            ranked = java.util.stream.IntStream.range(0, ranked.size()).boxed()
                    .sorted(Comparator.comparingInt((Integer index) -> rerankPositions.getOrDefault(index, Integer.MAX_VALUE))
                            .thenComparing((Integer index) -> rrfRanked.get(index).rrf, Comparator.reverseOrder()))
                    .map(rrfRanked::get).toList();
        }
        return ranked.stream().filter(item -> minScore <= 0D || item.vector >= minScore).limit(requestedTopK).map(item -> {
            KnowledgeChunkBO chunk = chunks.get(item.chunkId);
            return new HybridHit(item.chunkId, item.documentId != null ? item.documentId : chunk == null ? null : chunk.getDocumentId(),
                    chunk == null ? "" : chunk.getContent(), item.highlight, item.bm25, item.vector, item.rrf, item.rerank);
        }).toList();
    }

    private long requireTenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        return tenantId.value();
    }

    private List<HybridHit> keywordHits(TenantId tenantId, List<FullTextHit> textHits, int topK, double threshold) {
        return textHits.stream().filter(hit -> threshold <= 0D || hit.score() >= threshold).limit(topK).map(hit -> {
            KnowledgeChunkBO chunk = chunkRepository.getById(tenantId, hit.chunkId());
            return new HybridHit(hit.chunkId(), hit.documentId(), chunk == null ? "" : chunk.getContent(), hit.highlight(), hit.score(), 0D, hit.score(), 0D);
        }).toList();
    }

    private HybridHit vectorHit(TenantId tenantId, VectorHit hit, Map<Long, KnowledgeChunkBO> chunks) {
        KnowledgeChunkBO chunk = chunks == null ? chunkRepository.getById(tenantId, hit.chunkId()) : chunks.get(hit.chunkId());
        return new HybridHit(hit.chunkId(), chunk == null ? null : chunk.getDocumentId(), chunk == null ? "" : chunk.getContent(),
                null, 0D, hit.score(), hit.score(), 0D);
    }

    // Compatibility bridges keep existing package-level reflection tests stable.
    private void writeVectorManifest(Path vectorPath, int dimension) throws Exception { storage.writeVectorManifest(vectorPath, dimension); }
    private EmbeddedIndexStorage.VectorManifest readVectorManifest(Path vectorPath) throws Exception { return storage.readVectorManifest(vectorPath); }
    private float[] fromBytes(byte[] bytes) { return storage.fromBytes(bytes); }
    private Long parseVersion(String value) { return storage.parseVersion(value); }
    private String resolveAppHome(String value) { return storage.resolveAppHome(value); }
    private void cleanupOldVersions(Long tenantId, Long spaceId, long version) { storage.cleanupOldVersions(tenantId, spaceId, version); }
    private void closeQuietly(AutoCloseable resource) { storage.closeQuietly(resource); }

    @PreDestroy
    public void closeAll() { storage.closeAll(); }

    record IndexKey(Long tenantId, Long spaceId, Long version) { }

    private static final class MutableHit {
        private final Long chunkId;
        private Long documentId;
        private String highlight;
        private double bm25;
        private double vector;
        private double rrf;
        private double rerank;
        private MutableHit(Long chunkId) { this.chunkId = chunkId; }
    }
}
