package com.shiyu.ai.knowledge.implementation.infrastructure.index.adapter;

import com.shiyu.ai.knowledge.implementation.infrastructure.index.adapter.EmbeddedIndexRegistry.IndexKey;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.api.VectorStoreProvider;
import com.shiyu.ai.common.vector.model.VectorRecord;
import com.shiyu.ai.common.vector.model.VectorStoreOptions;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeChunkBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentBO;

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
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.BooleanSupplier;

/**
 * 负责 Embedded 索引 相关数据的存储或后台处理。
 */
@Slf4j
final class EmbeddedIndexStorage {
    /**
     * VECTOR_MANIFEST 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final String VECTOR_MANIFEST = "manifest.properties";
    /**
     * 向量存储提供者，表示当前对象中的对应属性。
     */
    private final VectorStoreProvider vectorStoreProvider;
    /**
     * 索引根目录，表示当前对象中的对应属性。
     */
    private final Path indexRoot;
    /**
     * rollbackVersions 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int rollbackVersions;
    /**
     * handles 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Cache<IndexKey, IndexHandle> handles;

    EmbeddedIndexStorage(
            VectorStoreProvider vectorStoreProvider,
            String dataDir,
            long idleMinutes,
            int rollbackVersions) {
        this.vectorStoreProvider = vectorStoreProvider;
        this.indexRoot = Path.of(resolveAppHome(dataDir), "index");
        this.rollbackVersions = Math.max(0, rollbackVersions);
        this.handles =
                Caffeine.<IndexKey, IndexHandle>newBuilder()
                        .expireAfterAccess(Duration.ofMinutes(Math.max(1, idleMinutes)))
                        .removalListener(
                                (RemovalListener<IndexKey, IndexHandle>)
                                        (key, handle, cause) -> close(handle))
                        .build();
    }

    Path path(Long tenantId, Long spaceId, Long version) {
        return indexRoot
                .resolve(String.valueOf(tenantId))
                .resolve(String.valueOf(spaceId))
                .resolve(String.valueOf(version));
    }

    Path stagingPath(Long tenantId, Long spaceId, Long fencingVersion) {
        return indexRoot
                .resolve(String.valueOf(tenantId))
                .resolve(String.valueOf(spaceId))
                .resolve(".staging")
                .resolve(String.valueOf(fencingVersion));
    }

    Path stagingLockPath(Long tenantId, Long spaceId, Long fencingVersion) {
        return indexRoot
                .resolve(String.valueOf(tenantId))
                .resolve(String.valueOf(spaceId))
                .resolve(".staging")
                .resolve(".locks")
                .resolve(fencingVersion + ".lock");
    }

    void invalidate(IndexKey key) {
        handles.invalidate(key);
    }

    void buildLucene(
            Path directoryPath,
            List<KnowledgeChunkBO> chunks,
            Map<Long, KnowledgeDocumentBO> documents)
            throws IOException {
        buildLucene(directoryPath, chunks, documents, () -> true);
    }

    void buildLucene(
            Path directoryPath,
            List<KnowledgeChunkBO> chunks,
            Map<Long, KnowledgeDocumentBO> documents,
            BooleanSupplier leaseIsCurrent)
            throws IOException {
        Files.createDirectories(directoryPath);
        try (Directory directory = FSDirectory.open(directoryPath);
                Analyzer analyzer = new SmartChineseAnalyzer();
                IndexWriter writer =
                        new IndexWriter(
                                directory,
                                new IndexWriterConfig(analyzer)
                                        .setOpenMode(IndexWriterConfig.OpenMode.CREATE))) {
            for (KnowledgeChunkBO chunk : chunks) {
                ensureLeaseCurrent(leaseIsCurrent);
                KnowledgeDocumentBO source = documents.get(chunk.getDocumentId());
                Document document = new Document();
                document.add(
                        new StringField("chunkId", String.valueOf(chunk.getId()), Field.Store.YES));
                document.add(
                        new StringField(
                                "documentId",
                                String.valueOf(chunk.getDocumentId()),
                                Field.Store.YES));
                document.add(new LongPoint("documentIdPoint", chunk.getDocumentId()));
                document.add(
                        new TextField(
                                "title", source == null ? "" : source.getTitle(), Field.Store.YES));
                document.add(new TextField("content", chunk.getContent(), Field.Store.YES));
                writer.addDocument(document);
            }
            ensureLeaseCurrent(leaseIsCurrent);
            writer.commit();
        }
    }

    void buildVector(
            Path directoryPath,
            Long tenantId,
            Long spaceId,
            long version,
            List<KnowledgeChunkBO> chunks,
            long stagingVersion)
            throws IOException {
        buildVector(directoryPath, tenantId, spaceId, version, chunks, stagingVersion, () -> true);
    }

    void buildVector(
            Path directoryPath,
            Long tenantId,
            Long spaceId,
            long version,
            List<KnowledgeChunkBO> chunks,
            long stagingVersion,
            BooleanSupplier leaseIsCurrent)
            throws IOException {
        int dimension =
                chunks.stream()
                        .map(KnowledgeChunkBO::getEmbeddingDimension)
                        .filter(value -> value != null && value > 0)
                        .findFirst()
                        .orElse(512);
        Files.createDirectories(directoryPath);
        String namespace =
                "knowledge/" + tenantId + "/" + spaceId + "/" + version + "/attempt/" + stagingVersion;
        VectorStoreOptions stagingOptions =
                vectorOptions(namespace, dimension, directoryPath);
        writeVectorManifest(directoryPath, dimension, namespace);
        try (VectorStore store = vectorStoreProvider.open(stagingOptions)) {
            for (KnowledgeChunkBO chunk : chunks) {
                ensureLeaseCurrent(leaseIsCurrent);
                if (chunk.getEmbeddingBinary() != null) {
                    store.upsert(
                            new VectorRecord(
                                    String.valueOf(chunk.getId()),
                                    fromBytes(chunk.getEmbeddingBinary()),
                                    Map.of("documentId", chunk.getDocumentId())));
                }
            }
            store.flush();
            writeVectorManifest(directoryPath, dimension, namespace);
        } catch (IOException | RuntimeException exception) {
            vectorStoreProvider.drop(stagingOptions);
            throw exception;
        }
    }

    IndexHandle handle(IndexKey key) {
        try {
            return handles.get(key, this::open);
        } catch (RuntimeException exception) {
            throw new com.shiyu.ai.common.foundation.exception.ServiceException(
                    "索引版本不可用: " + key.version());
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
                throw new IllegalStateException(
                        "Vector provider changed from "
                                + manifest.provider()
                                + " to "
                                + vectorStoreProvider.type()
                                + "; rebuild the space index");
            }
            vectorStore =
                    vectorStoreProvider.open(
                            vectorOptions(
                                    manifest.namespace(),
                                    manifest.dimension(),
                                    vectorPath));
            return new IndexHandle(
                    directory, reader, new IndexSearcher(reader), analyzer, vectorStore);
        } catch (Exception exception) {
            closeQuietly(vectorStore);
            closeQuietly(reader);
            closeQuietly(directory);
            closeQuietly(analyzer);
            throw new IllegalStateException(exception);
        }
    }

    void cleanupAbandonedStaging(Long tenantId, Long spaceId, long activeFencingVersion) {
        cleanupAbandonedStaging(tenantId, spaceId, activeFencingVersion, () -> true);
    }

    void cleanupAbandonedStaging(
            Long tenantId,
            Long spaceId,
            long activeFencingVersion,
            BooleanSupplier leaseIsCurrent) {
        Path stagingRoot =
                indexRoot.resolve(String.valueOf(tenantId))
                        .resolve(String.valueOf(spaceId))
                        .resolve(".staging");
        if (!Files.isDirectory(stagingRoot)) return;
        try (var paths = Files.list(stagingRoot)) {
            paths.filter(Files::isDirectory)
                    .forEach(
                            stagingPath -> {
                                Long fencingVersion = parseVersion(stagingPath.getFileName().toString());
                                if (fencingVersion == null || fencingVersion >= activeFencingVersion) return;
                                Path stagingLockPath = stagingLockPath(tenantId, spaceId, fencingVersion);
                                try {
                                    Files.createDirectories(stagingLockPath.getParent());
                                    try (FileChannel channel =
                                                    FileChannel.open(
                                                            stagingLockPath,
                                                            StandardOpenOption.CREATE,
                                                            StandardOpenOption.WRITE)) {
                                        var lock = channel.tryLock();
                                        if (lock == null) return;
                                        try (lock) {
                                            ensureLeaseCurrent(leaseIsCurrent);
                                            Path vectorPath = stagingPath.resolve("vector");
                                            if (dropVectorStore(
                                                    tenantId, spaceId, fencingVersion, vectorPath)) {
                                                deleteTree(stagingPath);
                                            }
                                        }
                                    }
                                } catch (java.nio.channels.OverlappingFileLockException inUse) {
                                    return;
                                } catch (IOException exception) {
                                    log.debug("Unable to lock abandoned staging index", exception);
                                } catch (RuntimeException leaseLost) {
                                    return;
                                }
                            });
        } catch (IOException exception) {
            log.warn(
                    "Failed to clean abandoned staging indexes: tenantSelected={}, spacePresent={}, errorType={}",
                    tenantId != null,
                    spaceId != null,
                    exception.getClass().getSimpleName());
        }
        try (var remaining = Files.list(stagingRoot)) {
            if (remaining.findAny().isEmpty()) Files.deleteIfExists(stagingRoot);
        } catch (IOException exception) {
            log.debug("Unable to remove empty index staging directory", exception);
        }
    }

    void cleanupOldVersions(Long tenantId, Long spaceId, long activeVersion) {
        Path spaceRoot =
                indexRoot.resolve(String.valueOf(tenantId)).resolve(String.valueOf(spaceId));
        if (!Files.isDirectory(spaceRoot)) return;
        java.util.Set<Long> keep = new java.util.LinkedHashSet<>();
        keep.add(activeVersion);
        try (var versions = Files.list(spaceRoot)) {
            List<Long> previousVersions =
                    versions.filter(Files::isDirectory)
                            .map(path -> parseVersion(path.getFileName().toString()))
                            .filter(Objects::nonNull)
                            .filter(version -> version < activeVersion)
                            .sorted(Comparator.reverseOrder())
                            .limit(rollbackVersions)
                            .toList();
            keep.addAll(previousVersions);
            try (var paths = Files.list(spaceRoot)) {
                paths.filter(Files::isDirectory)
                        .forEach(
                                versionPath -> {
                                    Long version =
                                            parseVersion(versionPath.getFileName().toString());
                                    if (version != null && !keep.contains(version)) {
                                        handles.invalidate(
                                                new IndexKey(tenantId, spaceId, version));
                                        if (dropVectorStore(
                                                tenantId,
                                                spaceId,
                                                version,
                                                versionPath.resolve("vector"))) {
                                            deleteTree(versionPath);
                                        }
                                    }
                                });
            }
        } catch (IOException exception) {
            log.warn(
                    "Failed to clean old embedded indexes: tenantSelected={}, spacePresent={},"
                            + " errorType={}, errorMessageLength={}",
                    tenantId != null,
                    spaceId != null,
                    exception.getClass().getSimpleName(),
                    exception.getMessage() == null ? 0 : exception.getMessage().length());
        }
    }

    void deleteTree(Path root) {
        if (root == null || !Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(
                            path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException exception) {
                                    log.debug("Unable to delete old index path", exception);
                                }
                            });
        } catch (IOException exception) {
            log.debug("Unable to enumerate old index path", exception);
        }
    }

    void writeVectorManifest(Path vectorPath, int dimension, String namespace) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("provider", vectorStoreProvider.type());
        properties.setProperty("dimension", String.valueOf(dimension));
        properties.setProperty("namespace", namespace);
        try (var output = Files.newOutputStream(vectorPath.resolve(VECTOR_MANIFEST))) {
            properties.store(output, "Shiyu vector index manifest");
        }
    }

    VectorManifest readVectorManifest(Path vectorPath) throws IOException {
        Path manifestPath = vectorPath.resolve(VECTOR_MANIFEST);
        if (!Files.exists(manifestPath))
            throw new IOException("Vector index manifest is missing: " + manifestPath);
        Properties properties = new Properties();
        try (var input = Files.newInputStream(manifestPath)) {
            properties.load(input);
        }
        String provider = properties.getProperty("provider");
        String namespace = properties.getProperty("namespace");
        int dimension;
        try {
            dimension = Integer.parseInt(properties.getProperty("dimension", "0"));
        } catch (NumberFormatException exception) {
            throw new IOException("Invalid vector dimension in " + manifestPath, exception);
        }
        if (provider == null || provider.isBlank() || dimension <= 0)
            throw new IOException("Invalid vector index manifest: " + manifestPath);
        if (namespace == null || namespace.isBlank()) {
            Path versionPath = vectorPath.getParent();
            Path spacePath = versionPath.getParent();
            namespace =
                    "knowledge/" + spacePath.getParent().getFileName()
                            + "/" + spacePath.getFileName()
                            + "/" + versionPath.getFileName();
        }
        return new VectorManifest(provider, dimension, namespace);
    }

    boolean dropVectorStore(Long tenantId, Long spaceId, long version, Path vectorPath) {
        if (vectorPath == null || !Files.exists(vectorPath.resolve(VECTOR_MANIFEST))) return true;
        try {
            VectorManifest manifest = readVectorManifest(vectorPath);
            vectorStoreProvider.drop(
                    vectorOptions(manifest.namespace(), manifest.dimension(), vectorPath));
            return true;
        } catch (IOException | RuntimeException exception) {
            log.warn(
                    "Unable to drop old index vector namespace: tenantSelected={}, spacePresent={},"
                            + " versionPresent={}, errorType={}",
                    tenantId != null,
                    spaceId != null,
                    version > 0,
                    exception.getClass().getSimpleName());
            return false;
        }
    }

    VectorStoreOptions vectorOptions(String namespace, int dimension, Path directoryPath) {
        return VectorStoreOptions.of(namespace, dimension, directoryPath.toString());
    }


    float[] fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        float[] vector = new float[bytes.length / Float.BYTES];
        for (int i = 0; i < vector.length; i++) vector[i] = buffer.getFloat();
        return vector;
    }

    Long parseVersion(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    String resolveAppHome(String value) {
        return value.replace("${app.home}", System.getProperty("app.home", "."));
    }

    void closeQuietly(AutoCloseable resource) {
        if (resource == null) return;
        try {
            resource.close();
        } catch (Exception exception) {
            log.debug("Unable to close partially opened index resource", exception);
        }
    }

    void closeAll() {
        // Caffeine 的移除通知可能异步调度；先同步关闭现有句柄，避免关闭时与目录清理竞态。
        handles.asMap().values().forEach(this::close);
        handles.invalidateAll();
        handles.cleanUp();
    }

    private static void ensureLeaseCurrent(BooleanSupplier leaseIsCurrent) {
        if (!leaseIsCurrent.getAsBoolean()) {
            throw new IllegalStateException("Index rebuild lease was lost");
        }
    }

    private void close(IndexHandle handle) {
        if (handle == null) return;
        try {
            handle.vectorStore().close();
            handle.reader().close();
            handle.directory().close();
            handle.analyzer().close();
        } catch (IOException exception) {
            log.warn("Failed to close embedded index", exception);
        }
    }

    /**
     * 封装 向量 Manifest 相关的不可变数据及其字段约束。
     */
    record VectorManifest(String provider, int dimension, String namespace) {}

    /**
     * 封装 索引 Handle 相关的不可变数据及其字段约束。
     */
    record IndexHandle(
            Directory directory,
            DirectoryReader reader,
            IndexSearcher searcher,
            Analyzer analyzer,
            VectorStore vectorStore) {}
}
