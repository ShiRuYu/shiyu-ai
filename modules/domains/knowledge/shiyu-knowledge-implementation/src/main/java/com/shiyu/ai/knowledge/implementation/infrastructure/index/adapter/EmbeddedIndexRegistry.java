package com.shiyu.ai.knowledge.implementation.infrastructure.index.adapter;


import com.shiyu.ai.knowledge.implementation.infrastructure.index.port.FullTextIndex.FullTextHit;
import com.shiyu.ai.knowledge.implementation.infrastructure.index.port.VectorIndex.VectorHit;
import com.shiyu.ai.knowledge.implementation.infrastructure.index.service.KnowledgeIndexService;
import com.shiyu.ai.knowledge.implementation.infrastructure.index.service.KnowledgeIndexService.HybridHit;

import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.common.vector.api.VectorStoreProvider;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.port.provider.EmbeddingProvider;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeChunkBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.implementation.domain.port.provider.RerankProvider;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeChunkRepository;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;

import jakarta.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
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

/**
 * 管理 Embedded 索引 相关的运行时状态、注册信息或临时数据。
 */
@Slf4j
@Service
@SuppressWarnings("deprecation")
public class EmbeddedIndexRegistry implements KnowledgeIndexService {
    /**
     * enterpriseRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeEnterpriseRepository enterpriseRepository;
    /**
     * documentRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeDocumentRepository documentRepository;
    /**
     * chunkRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeChunkRepository chunkRepository;
    /**
     * 嵌入向量提供者，表示当前对象中的对应属性。
     */
    private final EmbeddingProvider embeddingProvider;
    /**
     * rerankProvider 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final RerankProvider rerankProvider;
    /**
     * storage 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final EmbeddedIndexStorage storage;
    /**
     * rrfK 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int rrfK;

    /**
     * 执行 Embedded 索引 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param enterpriseRepository 用于完成本次业务处理的 enterpriseRepository 参数。
     * @param documentRepository 用于完成本次业务处理的 documentRepository 参数。
     * @param chunkRepository 用于完成本次业务处理的 chunkRepository 参数。
     * @param embeddingProvider 用于完成本次业务处理的 embeddingProvider 参数。
     * @param rerankProvider 用于完成本次业务处理的 rerankProvider 参数。
     * @param vectorStoreProvider 用于完成本次业务处理的 vectorStoreProvider 参数。
     * @param dataDir 用于完成本次业务处理的 dataDir 参数。
     * @param idleMinutes 用于完成本次业务处理的 idleMinutes 参数。
     * @param rrfK 用于完成本次业务处理的 rrfK 参数。
     * @param rollbackVersions 用于完成本次业务处理的 rollbackVersions 参数。
     */
    public EmbeddedIndexRegistry(
            KnowledgeEnterpriseRepository enterpriseRepository,
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
        this.storage =
                new EmbeddedIndexStorage(
                        vectorStoreProvider, dataDir, idleMinutes, rollbackVersions);
        this.rrfK = rrfK;
    }

    /**
     * 执行 Embedded 索引 相关业务数据，并返回处理结果。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @return 返回 Embedded 索引 相关操作生成的结果数据。
     */
    @Override
    public synchronized long rebuild(TenantId tenant, Long spaceId) {
        long tenantId = requireTenant(tenant);
        KnowledgeSpaceBO space = enterpriseRepository.findSpaceByTenant(tenant, spaceId);
        if (space == null) throw new ServiceException("知识空间不存在: " + spaceId);
        if (!Long.valueOf(tenantId).equals(space.getTenantId()))
            throw new ServiceException("租户与知识空间不匹配");
        long previousVersion =
                space.getActiveIndexVersion() == null ? 0 : space.getActiveIndexVersion();
        long version = previousVersion + 1;
        Path versionPath = storage.path(tenantId, spaceId, version);
        try {
            Files.createDirectories(versionPath);
            List<KnowledgeDocumentBO> published =
                    documentRepository.findBySpace(tenant, spaceId).stream()
                            .filter(document -> "PUBLISHED".equals(document.getLifecycleStatus()))
                            .toList();
            Map<Long, KnowledgeDocumentBO> documents =
                    published.stream()
                            .collect(
                                    Collectors.toMap(
                                            KnowledgeDocumentBO::getId, Function.identity()));
            List<KnowledgeChunkBO> chunks =
                    chunkRepository.findBySpace(tenant, spaceId).stream()
                            .filter(chunk -> documents.containsKey(chunk.getDocumentId()))
                            .toList();
            storage.buildLucene(versionPath.resolve("lucene"), chunks, documents);
            storage.buildVector(versionPath.resolve("vector"), tenantId, spaceId, version, chunks);
            space.setActiveIndexVersion(version);
            enterpriseRepository.updateSpace(tenant, space);
            storage.invalidate(new IndexKey(tenantId, spaceId, version));
            storage.cleanupOldVersions(tenantId, spaceId, version);
            log.info(
                    "Activated embedded index tenantSelected={}, spacePresent={},"
                            + " versionPresent={}, chunks={}",
                    tenantId > 0,
                    spaceId != null,
                    version > 0,
                    chunks.size());
            return version;
        } catch (Exception exception) {
            storage.deleteTree(versionPath);
            log.error(
                    "构建嵌入式索引失败: tenantSelected={}, spacePresent={}, versionPresent={},"
                            + " errorType={}, errorMessageLength={}",
                    tenantId > 0,
                    spaceId != null,
                    version > 0,
                    exception.getClass().getSimpleName(),
                    exception.getMessage() == null ? 0 : exception.getMessage().length());
            throw new ServiceException("构建嵌入式索引失败");
        }
    }

    /**
     * 查询 Embedded 索引 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param version 用于完成本次业务处理的 version 参数。
     * @param queryText 用于完成本次业务处理的 queryText 参数。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<FullTextHit> search(
            TenantId tenantId, Long spaceId, Long version, String queryText, int topK) {
        EmbeddedIndexStorage.IndexHandle handle =
                storage.handle(new IndexKey(requireTenant(tenantId), spaceId, version));
        try {
            Query query =
                    new MultiFieldQueryParser(new String[] {"title", "content"}, handle.analyzer())
                            .parse(MultiFieldQueryParser.escape(queryText));
            TopDocs topDocs = handle.searcher().search(query, topK);
            UnifiedHighlighter highlighter =
                    new UnifiedHighlighter(handle.searcher(), handle.analyzer());
            String[] highlights = highlighter.highlight("content", query, topDocs, 1);
            List<FullTextHit> hits = new ArrayList<>();
            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                var scoreDoc = topDocs.scoreDocs[i];
                Document document = handle.searcher().storedFields().document(scoreDoc.doc);
                hits.add(
                        new FullTextHit(
                                Long.parseLong(document.get("chunkId")),
                                Long.parseLong(document.get("documentId")),
                                scoreDoc.score,
                                i < highlights.length ? highlights[i] : document.get("content")));
            }
            return hits;
        } catch (Exception exception) {
            log.error(
                    "全文检索失败: tenantSelected={}, spacePresent={}, versionPresent={}, errorType={},"
                            + " errorMessageLength={}",
                    tenantId != null,
                    spaceId != null,
                    version != null,
                    exception.getClass().getSimpleName(),
                    exception.getMessage() == null ? 0 : exception.getMessage().length());
            throw new ServiceException("全文检索失败，请稍后重试");
        }
    }

    /**
     * 查询 Embedded 索引 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param version 用于完成本次业务处理的 version 参数。
     * @param queryVector 用于完成本次业务处理的 queryVector 参数。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<VectorHit> search(
            TenantId tenantId, Long spaceId, Long version, float[] queryVector, int topK) {
        return storage
                .handle(new IndexKey(requireTenant(tenantId), spaceId, version))
                .vectorStore()
                .search(queryVector, topK)
                .stream()
                .map(
                        record ->
                                new VectorHit(
                                        Long.parseLong(record.id()),
                                        ((Number) record.metadata().getOrDefault("_score", 0D))
                                                .doubleValue()))
                .toList();
    }

    /**
     * 执行 Embedded 索引 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param query 用于筛选目标数据的查询条件。
     * @param mode 用于完成本次业务处理的 mode 参数。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @param threshold 用于完成本次业务处理的 threshold 参数。
     * @param rerank 用于完成本次业务处理的 rerank 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<HybridHit> hybridSearch(
            TenantId tenantId,
            Long spaceId,
            String query,
            String mode,
            int topK,
            double threshold,
            boolean rerank) {
        requireTenant(tenantId);
        return hybridSearchInternal(null, tenantId, spaceId, query, mode, topK, threshold, rerank);
    }

    /**
     * 执行 Embedded 索引 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     * @param query 用于筛选目标数据的查询条件。
     * @param mode 用于完成本次业务处理的 mode 参数。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @param threshold 用于完成本次业务处理的 threshold 参数。
     * @param rerank 用于完成本次业务处理的 rerank 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<HybridHit> hybridSearch(
            ActorContext actor,
            Long spaceId,
            String query,
            String mode,
            int topK,
            double threshold,
            boolean rerank) {
        if (actor == null) throw new IllegalArgumentException("actor is required");
        return hybridSearchInternal(
                actor, actor.tenantId(), spaceId, query, mode, topK, threshold, rerank);
    }

    private List<HybridHit> hybridSearchInternal(
            ActorContext actor,
            TenantId tenantId,
            Long spaceId,
            String query,
            String mode,
            int topK,
            double threshold,
            boolean rerank) {
        requireTenant(tenantId);
        KnowledgeSpaceBO space = enterpriseRepository.findSpaceByTenant(tenantId, spaceId);
        if (space == null
                || space.getActiveIndexVersion() == null
                || space.getActiveIndexVersion() <= 0) return List.of();
        String normalizedMode =
                mode == null || mode.isBlank()
                        ? "HYBRID"
                        : mode.trim().toUpperCase(java.util.Locale.ROOT);
        if (!Set.of("KEYWORD", "SEMANTIC", "VECTOR", "HYBRID").contains(normalizedMode)) {
            throw new ServiceException("不支持的检索模式: " + mode);
        }
        int requestedTopK = Math.max(1, Math.min(100, topK));
        double minScore = Math.max(0D, threshold);
        int candidates = Math.max(20, requestedTopK);
        List<FullTextHit> textHits =
                search(tenantId, spaceId, space.getActiveIndexVersion(), query, candidates);
        if ("KEYWORD".equals(normalizedMode))
            return keywordHits(tenantId, textHits, requestedTopK, minScore);
        List<VectorHit> vectorHits;
        try {
            float[] queryVector =
                    actor == null
                            ? embeddingProvider.embed(tenantId, query)
                            : embeddingProvider.embed(actor, query);
            vectorHits =
                    search(
                            tenantId,
                            spaceId,
                            space.getActiveIndexVersion(),
                            queryVector,
                            candidates);
        } catch (IllegalStateException exception) {
            if ("HYBRID".equals(normalizedMode)) {
                log.warn(
                        "向量检索未启用，HYBRID 降级为全文检索: errorType={}, errorMessageLength={}",
                        exception.getClass().getSimpleName(),
                        exception.getMessage() == null ? 0 : exception.getMessage().length());
                return keywordHits(tenantId, textHits, requestedTopK, minScore);
            }
            throw new ServiceException("向量检索未启用，请配置 Embedding 模型或使用 KEYWORD/HYBRID 模式");
        }
        if ("SEMANTIC".equals(normalizedMode) || "VECTOR".equals(normalizedMode)) {
            return vectorHits.stream()
                    .filter(hit -> minScore <= 0D || hit.score() >= minScore)
                    .limit(requestedTopK)
                    .map(hit -> vectorHit(tenantId, hit, null))
                    .toList();
        }
        Map<Long, MutableHit> merged = new LinkedHashMap<>();
        for (int i = 0; i < textHits.size(); i++) {
            FullTextHit hit = textHits.get(i);
            MutableHit item = merged.computeIfAbsent(hit.chunkId(), MutableHit::new);
            item.documentId = hit.documentId();
            item.highlight = hit.highlight();
            item.bm25 = hit.score();
            item.rrf += 1D / (rrfK + i + 1);
        }
        for (int i = 0; i < vectorHits.size(); i++) {
            VectorHit hit = vectorHits.get(i);
            MutableHit item = merged.computeIfAbsent(hit.chunkId(), MutableHit::new);
            item.vector = hit.score();
            item.rrf += 1D / (rrfK + i + 1);
        }
        Map<Long, KnowledgeChunkBO> chunks =
                chunkRepository.findBySpace(tenantId, spaceId).stream()
                        .collect(Collectors.toMap(KnowledgeChunkBO::getId, Function.identity()));
        List<MutableHit> ranked = new ArrayList<>(merged.values());
        ranked.sort(Comparator.comparingDouble((MutableHit item) -> item.rrf).reversed());
        if (rerank && !ranked.isEmpty()) {
            List<MutableHit> rrfRanked = ranked;
            List<String> contents =
                    ranked.stream()
                            .map(
                                    item -> {
                                        KnowledgeChunkBO chunk = chunks.get(item.chunkId);
                                        return chunk == null ? "" : chunk.getContent();
                                    })
                            .toList();
            List<Integer> order =
                    rerankProvider.rerank(query, contents, Math.min(topK, ranked.size()));
            Map<Integer, Integer> rerankPositions = new HashMap<>();
            for (int i = 0; i < order.size(); i++) {
                Integer candidateIndex = order.get(i);
                if (candidateIndex != null
                        && candidateIndex >= 0
                        && candidateIndex < ranked.size()) {
                    rerankPositions.putIfAbsent(candidateIndex, i);
                    ranked.get(candidateIndex).rerank = 1D - (double) i / Math.max(1, order.size());
                }
            }
            ranked =
                    java.util.stream.IntStream.range(0, ranked.size())
                            .boxed()
                            .sorted(
                                    Comparator.comparingInt(
                                                    (Integer index) ->
                                                            rerankPositions.getOrDefault(
                                                                    index, Integer.MAX_VALUE))
                                            .thenComparing(
                                                    (Integer index) -> rrfRanked.get(index).rrf,
                                                    Comparator.reverseOrder()))
                            .map(rrfRanked::get)
                            .toList();
        }
        return ranked.stream()
                .filter(item -> minScore <= 0D || item.vector >= minScore)
                .limit(requestedTopK)
                .map(
                        item -> {
                            KnowledgeChunkBO chunk = chunks.get(item.chunkId);
                            return new HybridHit(
                                    item.chunkId,
                                    item.documentId != null
                                            ? item.documentId
                                            : chunk == null ? null : chunk.getDocumentId(),
                                    chunk == null ? "" : chunk.getContent(),
                                    item.highlight,
                                    item.bm25,
                                    item.vector,
                                    item.rrf,
                                    item.rerank);
                        })
                .toList();
    }

    private long requireTenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        return tenantId.value();
    }

    private List<HybridHit> keywordHits(
            TenantId tenantId, List<FullTextHit> textHits, int topK, double threshold) {
        return textHits.stream()
                .filter(hit -> threshold <= 0D || hit.score() >= threshold)
                .limit(topK)
                .map(
                        hit -> {
                            KnowledgeChunkBO chunk =
                                    chunkRepository.getById(tenantId, hit.chunkId());
                            return new HybridHit(
                                    hit.chunkId(),
                                    hit.documentId(),
                                    chunk == null ? "" : chunk.getContent(),
                                    hit.highlight(),
                                    hit.score(),
                                    0D,
                                    hit.score(),
                                    0D);
                        })
                .toList();
    }

    private HybridHit vectorHit(
            TenantId tenantId, VectorHit hit, Map<Long, KnowledgeChunkBO> chunks) {
        KnowledgeChunkBO chunk =
                chunks == null
                        ? chunkRepository.getById(tenantId, hit.chunkId())
                        : chunks.get(hit.chunkId());
        return new HybridHit(
                hit.chunkId(),
                chunk == null ? null : chunk.getDocumentId(),
                chunk == null ? "" : chunk.getContent(),
                null,
                0D,
                hit.score(),
                hit.score(),
                0D);
    }

    private void writeVectorManifest(Path vectorPath, int dimension) throws Exception {
        storage.writeVectorManifest(vectorPath, dimension);
    }

    private EmbeddedIndexStorage.VectorManifest readVectorManifest(Path vectorPath)
            throws Exception {
        return storage.readVectorManifest(vectorPath);
    }

    private float[] fromBytes(byte[] bytes) {
        return storage.fromBytes(bytes);
    }

    private Long parseVersion(String value) {
        return storage.parseVersion(value);
    }

    private String resolveAppHome(String value) {
        return storage.resolveAppHome(value);
    }

    private void cleanupOldVersions(Long tenantId, Long spaceId, long version) {
        storage.cleanupOldVersions(tenantId, spaceId, version);
    }

    private void closeQuietly(AutoCloseable resource) {
        storage.closeQuietly(resource);
    }

    /**
     * {@code closeAll} 释放或移除当前操作涉及的资源。
     */
    @PreDestroy
    public void closeAll() {
        storage.closeAll();
    }

    /**
     * 封装 索引 Key 相关的不可变数据及其字段约束。
     */
    record IndexKey(Long tenantId, Long spaceId, Long version) {}

    /**
     * 提供 Mutable Hit 所属基础设施的适配、存储或运行支持。
     */
    private static final class MutableHit {
        private final Long chunkId;
        /**
         * documentId 属性，保存当前对象中的业务数据或协作依赖。
         */
        private Long documentId;
        /**
         * highlight 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String highlight;
        /**
         * bm25 属性，保存当前对象中的业务数据或协作依赖。
         */
        private double bm25;
        /**
         * 向量，表示当前对象中的对应属性。
         */
        private double vector;
        /**
         * rrf 属性，保存当前对象中的业务数据或协作依赖。
         */
        private double rrf;
        /**
         * rerank 属性，保存当前对象中的业务数据或协作依赖。
         */
        private double rerank;

        private MutableHit(Long chunkId) {
            this.chunkId = chunkId;
        }
    }
}
