package com.shiyu.ai.knowledge.index;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeChunkBO;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeDocumentBO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeSpaceDO;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeChunkRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.model.EmbeddingProvider;
import com.shiyu.ai.knowledge.model.RerankProvider;
import com.shiyu.ai.vector.VectorRecord;
import com.shiyu.ai.vector.config.VectorStoreProperties;
import com.shiyu.ai.vector.impl.JVectorStore;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmbeddedIndexRegistry implements KnowledgeIndexService {

    private final KnowledgeEnterpriseRepository enterpriseRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final EmbeddingProvider embeddingProvider;
    private final RerankProvider rerankProvider;
    private final Path indexRoot;
    private final int rrfK;
    private final Cache<IndexKey, IndexHandle> handles;

    public EmbeddedIndexRegistry(KnowledgeEnterpriseRepository enterpriseRepository,
                                 KnowledgeDocumentRepository documentRepository,
                                 KnowledgeChunkRepository chunkRepository,
                                 EmbeddingProvider embeddingProvider,
                                 RerankProvider rerankProvider,
                                 @Value("${shiyu.knowledge.data-dir:${app.home}/data}") String dataDir,
                                 @Value("${shiyu.knowledge.index.idle-minutes:15}") long idleMinutes,
                                 @Value("${shiyu.knowledge.index.rrf-k:60}") int rrfK) {
        this.enterpriseRepository = enterpriseRepository;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingProvider = embeddingProvider;
        this.rerankProvider = rerankProvider;
        this.indexRoot = Path.of(resolveAppHome(dataDir), "index");
        this.rrfK = rrfK;
        this.handles = Caffeine.<IndexKey, IndexHandle>newBuilder()
                .expireAfterAccess(Duration.ofMinutes(Math.max(1, idleMinutes)))
                .removalListener((RemovalListener<IndexKey, IndexHandle>)
                        (key, handle, cause) -> close(handle))
                .build();
    }

    @Override
    public synchronized long rebuild(Long tenantId, Long spaceId) {
        KnowledgeSpaceDO space = enterpriseRepository.findSpace(spaceId);
        if (space == null) throw new ServiceException("知识空间不存在: " + spaceId);
        long version = (space.getActiveIndexVersion() == null ? 0 : space.getActiveIndexVersion()) + 1;
        Path versionPath = path(tenantId, spaceId, version);
        try {
            Files.createDirectories(versionPath);
            List<KnowledgeDocumentBO> published = documentRepository.findBySpace(spaceId).stream()
                    .filter(document -> "PUBLISHED".equals(document.getLifecycleStatus()))
                    .toList();
            Map<Long, KnowledgeDocumentBO> documents = published.stream()
                    .collect(Collectors.toMap(KnowledgeDocumentBO::getId, Function.identity()));
            List<KnowledgeChunkBO> chunks = chunkRepository.findBySpace(spaceId).stream()
                    .filter(chunk -> documents.containsKey(chunk.getDocumentId()))
                    .toList();
            buildLucene(versionPath.resolve("lucene"), chunks, documents);
            buildVector(versionPath.resolve("jvector"), chunks);
            space.setActiveIndexVersion(version);
            enterpriseRepository.updateSpace(space);
            handles.invalidate(new IndexKey(tenantId, spaceId, version));
            log.info("Activated embedded index tenant={}, space={}, version={}, chunks={}",
                    tenantId, spaceId, version, chunks.size());
            return version;
        } catch (IOException exception) {
            throw new ServiceException("构建嵌入式索引失败: " + exception.getMessage());
        }
    }

    @Override
    public List<FullTextHit> search(Long tenantId, Long spaceId, Long version,
                                    String queryText, int topK) {
        IndexHandle handle = handle(tenantId, spaceId, version);
        try {
            Query query = new MultiFieldQueryParser(new String[]{"title", "content"},
                    handle.analyzer()).parse(MultiFieldQueryParser.escape(queryText));
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
            throw new ServiceException("全文检索失败: " + exception.getMessage());
        }
    }

    @Override
    public List<VectorHit> search(Long tenantId, Long spaceId, Long version,
                                  float[] queryVector, int topK) {
        return handle(tenantId, spaceId, version).vectorStore().search(queryVector, topK).stream()
                .map(record -> new VectorHit(Long.parseLong(record.id()),
                        ((Number) record.metadata().getOrDefault("_score", 0D)).doubleValue()))
                .toList();
    }

    @Override
    public List<HybridHit> hybridSearch(Long tenantId, Long spaceId, String query,
                                        int topK, boolean rerank) {
        KnowledgeSpaceDO space = enterpriseRepository.findSpace(spaceId);
        if (space == null || space.getActiveIndexVersion() == null
                || space.getActiveIndexVersion() <= 0) {
            return List.of();
        }
        int candidates = Math.max(20, topK);
        List<FullTextHit> textHits = search(tenantId, spaceId,
                space.getActiveIndexVersion(), query, candidates);
        List<VectorHit> vectorHits = search(tenantId, spaceId,
                space.getActiveIndexVersion(), embeddingProvider.embed(query), candidates);
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
        Map<Long, KnowledgeChunkBO> chunks = chunkRepository.findBySpace(spaceId).stream()
                .collect(Collectors.toMap(KnowledgeChunkBO::getId, Function.identity()));
        List<MutableHit> ranked = new ArrayList<>(merged.values());
        ranked.sort(Comparator.comparingDouble((MutableHit item) -> item.rrf).reversed());
        if (rerank && !ranked.isEmpty()) {
            List<MutableHit> rrfRanked = ranked;
            List<String> contents = ranked.stream()
                    .map(item -> {
                        KnowledgeChunkBO chunk = chunks.get(item.chunkId);
                        return chunk == null ? "" : chunk.getContent();
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
                    .sorted(Comparator
                            .comparingInt((Integer index) ->
                                    rerankPositions.getOrDefault(index, Integer.MAX_VALUE))
                            .thenComparing((Integer index) -> rrfRanked.get(index).rrf,
                                    Comparator.reverseOrder()))
                    .map(rrfRanked::get)
                    .toList();
        }
        return ranked.stream()
                .limit(topK)
                .map(item -> {
                    KnowledgeChunkBO chunk = chunks.get(item.chunkId);
                    return new HybridHit(item.chunkId,
                            item.documentId != null ? item.documentId
                                    : chunk == null ? null : chunk.getDocumentId(),
                            chunk == null ? "" : chunk.getContent(),
                            item.highlight, item.bm25, item.vector, item.rrf,
                            item.rerank);
                }).toList();
    }

    private void buildLucene(Path directoryPath, List<KnowledgeChunkBO> chunks,
                             Map<Long, KnowledgeDocumentBO> documents) throws IOException {
        Files.createDirectories(directoryPath);
        try (Directory directory = FSDirectory.open(directoryPath);
             Analyzer analyzer = new SmartChineseAnalyzer();
             IndexWriter writer = new IndexWriter(directory,
                     new IndexWriterConfig(analyzer).setOpenMode(IndexWriterConfig.OpenMode.CREATE))) {
            for (KnowledgeChunkBO chunk : chunks) {
                KnowledgeDocumentBO source = documents.get(chunk.getDocumentId());
                Document document = new Document();
                document.add(new StringField("chunkId", String.valueOf(chunk.getId()), Field.Store.YES));
                document.add(new StringField("documentId", String.valueOf(chunk.getDocumentId()), Field.Store.YES));
                document.add(new LongPoint("documentIdPoint", chunk.getDocumentId()));
                document.add(new TextField("title", source == null ? "" : source.getTitle(), Field.Store.YES));
                document.add(new TextField("content", chunk.getContent(), Field.Store.YES));
                writer.addDocument(document);
            }
            writer.commit();
        }
    }

    private void buildVector(Path directoryPath, List<KnowledgeChunkBO> chunks) {
        int dimension = chunks.stream().map(KnowledgeChunkBO::getEmbeddingDimension)
                .filter(value -> value != null && value > 0).findFirst().orElse(512);
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setType("jvector");
        properties.setDimension(dimension);
        properties.setDataDir(directoryPath.toString());
        JVectorStore store = new JVectorStore(properties);
        for (KnowledgeChunkBO chunk : chunks) {
            if (chunk.getEmbeddingBinary() != null) {
                store.upsert(new VectorRecord(String.valueOf(chunk.getId()),
                        fromBytes(chunk.getEmbeddingBinary()), Map.of("documentId", chunk.getDocumentId())));
            }
        }
        store.saveToDisk();
    }

    private IndexHandle handle(Long tenantId, Long spaceId, Long version) {
        try {
            return handles.get(new IndexKey(tenantId, spaceId, version), this::open);
        } catch (RuntimeException exception) {
            throw new ServiceException("索引版本不可用: " + version);
        }
    }

    private IndexHandle open(IndexKey key) {
        try {
            Path versionPath = path(key.tenantId(), key.spaceId(), key.version());
            Directory directory = FSDirectory.open(versionPath.resolve("lucene"));
            DirectoryReader reader = DirectoryReader.open(directory);
            Analyzer analyzer = new SmartChineseAnalyzer();
            int dimension = dimensionFromIndex(versionPath.resolve("jvector"));
            VectorStoreProperties properties = new VectorStoreProperties();
            properties.setType("jvector");
            properties.setDimension(dimension);
            properties.setDataDir(versionPath.resolve("jvector").toString());
            return new IndexHandle(directory, reader, new IndexSearcher(reader), analyzer,
                    new JVectorStore(properties));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private int dimensionFromIndex(Path vectorPath) throws IOException {
        Path file = vectorPath.resolve("hnsw.index");
        if (!Files.exists(file)) return 512;
        try (var input = new java.io.DataInputStream(Files.newInputStream(file))) {
            return input.readInt();
        }
    }

    private float[] fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        float[] vector = new float[bytes.length / Float.BYTES];
        for (int i = 0; i < vector.length; i++) vector[i] = buffer.getFloat();
        return vector;
    }

    private Path path(Long tenantId, Long spaceId, Long version) {
        return indexRoot.resolve(String.valueOf(tenantId))
                .resolve(String.valueOf(spaceId)).resolve(String.valueOf(version));
    }

    private String resolveAppHome(String value) {
        return value.replace("${app.home}", System.getProperty("app.home", "."));
    }

    private void close(IndexHandle handle) {
        if (handle == null) return;
        try {
            handle.vectorStore().saveToDisk();
            handle.reader().close();
            handle.directory().close();
            handle.analyzer().close();
        } catch (IOException exception) {
            log.warn("Failed to close embedded index", exception);
        }
    }

    private record IndexKey(Long tenantId, Long spaceId, Long version) {
    }

    private record IndexHandle(Directory directory, DirectoryReader reader,
                               IndexSearcher searcher, Analyzer analyzer,
                               JVectorStore vectorStore) {
    }

    private static final class MutableHit {
        private final Long chunkId;
        private Long documentId;
        private String highlight;
        private double bm25;
        private double vector;
        private double rrf;
        private double rerank;

        private MutableHit(Long chunkId) {
            this.chunkId = chunkId;
        }
    }
}
