package com.shiyu.ai.knowledge.implementation.infrastructure.index;

import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.api.VectorStoreProvider;
import com.shiyu.ai.common.vector.model.VectorRecord;
import com.shiyu.ai.common.vector.model.VectorStoreOptions;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeChunkBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.implementation.infrastructure.index.EmbeddedIndexRegistry.IndexKey;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/** Owns filesystem, Lucene, vector-store, and handle-cache lifecycle. */
@Slf4j
final class EmbeddedIndexStorage {
    private static final String VECTOR_MANIFEST = "manifest.properties";
    private final VectorStoreProvider vectorStoreProvider;
    private final Path indexRoot;
    private final int rollbackVersions;
    private final Cache<IndexKey, IndexHandle> handles;

    EmbeddedIndexStorage(VectorStoreProvider vectorStoreProvider, String dataDir,
                         long idleMinutes, int rollbackVersions) {
        this.vectorStoreProvider = vectorStoreProvider;
        this.indexRoot = Path.of(resolveAppHome(dataDir), "index");
        this.rollbackVersions = Math.max(0, rollbackVersions);
        this.handles = Caffeine.<IndexKey, IndexHandle>newBuilder()
                .expireAfterAccess(Duration.ofMinutes(Math.max(1, idleMinutes)))
                .removalListener((RemovalListener<IndexKey, IndexHandle>) (key, handle, cause) -> close(handle))
                .build();
    }

    Path path(Long tenantId, Long spaceId, Long version) {
        return indexRoot.resolve(String.valueOf(tenantId)).resolve(String.valueOf(spaceId)).resolve(String.valueOf(version));
    }

    void invalidate(IndexKey key) {
        handles.invalidate(key);
    }

    void buildLucene(Path directoryPath, List<KnowledgeChunkBO> chunks,
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

    void buildVector(Path directoryPath, Long tenantId, Long spaceId, long version,
                     List<KnowledgeChunkBO> chunks) throws IOException {
        int dimension = chunks.stream().map(KnowledgeChunkBO::getEmbeddingDimension)
                .filter(value -> value != null && value > 0).findFirst().orElse(512);
        Files.createDirectories(directoryPath);
        VectorStoreOptions options = vectorOptions(tenantId, spaceId, version, dimension, directoryPath);
        try (VectorStore store = vectorStoreProvider.open(options)) {
            for (KnowledgeChunkBO chunk : chunks) {
                if (chunk.getEmbeddingBinary() != null) {
                    store.upsert(new VectorRecord(String.valueOf(chunk.getId()),
                            fromBytes(chunk.getEmbeddingBinary()), Map.of("documentId", chunk.getDocumentId())));
                }
            }
            store.flush();
            writeVectorManifest(directoryPath, dimension);
        } catch (IOException | RuntimeException exception) {
            vectorStoreProvider.drop(options);
            throw exception;
        }
    }

    IndexHandle handle(IndexKey key) {
        try {
            return handles.get(key, this::open);
        } catch (RuntimeException exception) {
            throw new com.shiyu.ai.common.core.exception.ServiceException("索引版本不可用: " + key.version());
        }
    }

    private IndexHandle open(IndexKey key) {
        Directory directory = null;
        DirectoryReader reader = null;
        Analyzer analyzer = null;
        VectorStore vectorStore = null;
        try {
            Path versionPath = path(key.tenantId(), key.spaceId(), key.version());
            directory = FSDirectory.open(versionPath.resolve("lucene"));
            reader = DirectoryReader.open(directory);
            analyzer = new SmartChineseAnalyzer();
            Path vectorPath = versionPath.resolve("vector");
            VectorManifest manifest = readVectorManifest(vectorPath);
            if (!vectorStoreProvider.type().equalsIgnoreCase(manifest.provider())) {
                throw new IllegalStateException("Vector provider changed from " + manifest.provider()
                        + " to " + vectorStoreProvider.type() + "; rebuild the space index");
            }
            vectorStore = vectorStoreProvider.open(vectorOptions(key.tenantId(), key.spaceId(), key.version(),
                    manifest.dimension(), vectorPath));
            return new IndexHandle(directory, reader, new IndexSearcher(reader), analyzer, vectorStore);
        } catch (Exception exception) {
            closeQuietly(vectorStore); closeQuietly(reader); closeQuietly(directory); closeQuietly(analyzer);
            throw new IllegalStateException(exception);
        }
    }

    void cleanupOldVersions(Long tenantId, Long spaceId, long activeVersion) {
        Path spaceRoot = indexRoot.resolve(String.valueOf(tenantId)).resolve(String.valueOf(spaceId));
        if (!Files.isDirectory(spaceRoot)) return;
        java.util.Set<Long> keep = new java.util.LinkedHashSet<>();
        keep.add(activeVersion);
        try (var versions = Files.list(spaceRoot)) {
            List<Long> previousVersions = versions.filter(Files::isDirectory)
                    .map(path -> parseVersion(path.getFileName().toString())).filter(Objects::nonNull)
                    .filter(version -> version < activeVersion).sorted(Comparator.reverseOrder())
                    .limit(rollbackVersions).toList();
            keep.addAll(previousVersions);
            try (var paths = Files.list(spaceRoot)) {
                paths.filter(Files::isDirectory).forEach(versionPath -> {
                    Long version = parseVersion(versionPath.getFileName().toString());
                    if (version != null && !keep.contains(version)) {
                        handles.invalidate(new IndexKey(tenantId, spaceId, version));
                        dropVectorStore(tenantId, spaceId, version, versionPath.resolve("vector"));
                        deleteTree(versionPath);
                    }
                });
            }
        } catch (IOException exception) {
            log.warn("Failed to clean old embedded indexes: tenantSelected={}, spacePresent={}, errorType={}, errorMessageLength={}",
                    tenantId != null, spaceId != null, exception.getClass().getSimpleName(),
                    exception.getMessage() == null ? 0 : exception.getMessage().length());
        }
    }

    void deleteTree(Path root) {
        if (root == null || !Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try { Files.deleteIfExists(path); }
                catch (IOException exception) { log.debug("Unable to delete old index path", exception); }
            });
        } catch (IOException exception) { log.debug("Unable to enumerate old index path", exception); }
    }

    void writeVectorManifest(Path vectorPath, int dimension) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("provider", vectorStoreProvider.type());
        properties.setProperty("dimension", String.valueOf(dimension));
        try (var output = Files.newOutputStream(vectorPath.resolve(VECTOR_MANIFEST))) {
            properties.store(output, "Shiyu vector index manifest");
        }
    }

    VectorManifest readVectorManifest(Path vectorPath) throws IOException {
        Path manifestPath = vectorPath.resolve(VECTOR_MANIFEST);
        if (!Files.exists(manifestPath)) throw new IOException("Vector index manifest is missing: " + manifestPath);
        Properties properties = new Properties();
        try (var input = Files.newInputStream(manifestPath)) { properties.load(input); }
        String provider = properties.getProperty("provider");
        int dimension;
        try { dimension = Integer.parseInt(properties.getProperty("dimension", "0")); }
        catch (NumberFormatException exception) { throw new IOException("Invalid vector dimension in " + manifestPath, exception); }
        if (provider == null || provider.isBlank() || dimension <= 0) throw new IOException("Invalid vector index manifest: " + manifestPath);
        return new VectorManifest(provider, dimension);
    }

    void dropVectorStore(Long tenantId, Long spaceId, long version, Path vectorPath) {
        try {
            VectorManifest manifest = readVectorManifest(vectorPath);
            vectorStoreProvider.drop(vectorOptions(tenantId, spaceId, version, manifest.dimension(), vectorPath));
        } catch (IOException exception) { log.debug("Unable to read vector manifest while dropping index", exception); }
    }

    VectorStoreOptions vectorOptions(Long tenantId, Long spaceId, long version, int dimension, Path directoryPath) {
        return VectorStoreOptions.of("knowledge/" + tenantId + "/" + spaceId + "/" + version,
                dimension, directoryPath.toString());
    }

    float[] fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        float[] vector = new float[bytes.length / Float.BYTES];
        for (int i = 0; i < vector.length; i++) vector[i] = buffer.getFloat();
        return vector;
    }

    Long parseVersion(String value) {
        try { return Long.valueOf(value); } catch (NumberFormatException exception) { return null; }
    }

    String resolveAppHome(String value) {
        return value.replace("${app.home}", System.getProperty("app.home", "."));
    }

    void closeQuietly(AutoCloseable resource) {
        if (resource == null) return;
        try { resource.close(); } catch (Exception exception) { log.debug("Unable to close partially opened index resource", exception); }
    }

    void closeAll() {
        handles.invalidateAll();
        handles.cleanUp();
    }

    private void close(IndexHandle handle) {
        if (handle == null) return;
        try {
            handle.vectorStore().close();
            handle.reader().close();
            handle.directory().close();
            handle.analyzer().close();
        } catch (IOException exception) { log.warn("Failed to close embedded index", exception); }
    }

    record VectorManifest(String provider, int dimension) { }

    record IndexHandle(Directory directory, DirectoryReader reader, IndexSearcher searcher,
                       Analyzer analyzer, VectorStore vectorStore) { }
}
