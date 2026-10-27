package com.shiyu.ai.knowledge.implementation.application.service.rag;

import com.shiyu.ai.knowledge.implementation.application.port.rag.ChunkSplitter;

import com.shiyu.ai.knowledge.implementation.infrastructure.rag.ChineseChunkSplitter;

import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.application.port.document.DocumentParser;
import com.shiyu.ai.knowledge.implementation.application.port.rag.ChunkSplitter.Chunk;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeChunkBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentVersionBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeChunkRepository;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.model.contract.api.EmbeddingService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * 提供 文档 Ingestion 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
public class DocumentIngestionService {

    /**
     * 嵌入向量服务，表示当前对象中的对应属性。
     */
    private final EmbeddingService embeddingService;
    /**
     * chunkRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeChunkRepository chunkRepository;
    private final KnowledgeEnterpriseRepository enterpriseRepository;
    /**
     * chunkSplitter 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ChunkSplitter chunkSplitter;
    /**
     * documentParsers 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<DocumentParser> documentParsers;

    /**
     * 执行 文档 Ingestion 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param embeddingService 用于完成本次业务处理的 embeddingService 参数。
     * @param chunkRepository 用于完成本次业务处理的 chunkRepository 参数。
     * @param documentParsers 用于完成本次业务处理的 documentParsers 参数。
     */
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public DocumentIngestionService(
            EmbeddingService embeddingService,
            KnowledgeChunkRepository chunkRepository,
            KnowledgeEnterpriseRepository enterpriseRepository,
            @Qualifier("agentDataSource") javax.sql.DataSource dataSource,
            List<DocumentParser> documentParsers) {
        this.embeddingService = embeddingService;
        this.chunkRepository = chunkRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.transactionTemplate =
                new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        this.chunkSplitter = new ChineseChunkSplitter();
        this.documentParsers = documentParsers != null ? documentParsers : List.of();
    }

    public DocumentIngestionService(
            EmbeddingService embeddingService,
            KnowledgeChunkRepository chunkRepository,
            KnowledgeEnterpriseRepository enterpriseRepository,
            List<DocumentParser> documentParsers) {
        this.embeddingService = embeddingService;
        this.chunkRepository = chunkRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.transactionTemplate = null;
        this.chunkSplitter = new ChineseChunkSplitter();
        this.documentParsers = documentParsers != null ? documentParsers : List.of();
    }

    /** 保留兼容构造方法，供仅验证操作者的既有直接调用方使用。 */
    public DocumentIngestionService(
            EmbeddingService embeddingService,
            KnowledgeChunkRepository chunkRepository,
            List<DocumentParser> documentParsers) {
        this(embeddingService, chunkRepository, null, documentParsers);
    }

    /** 根据文件格式获取对应的文档解析器 */
    public Optional<DocumentParser> findParser(String format) {
        return documentParsers.stream()
                .filter(p -> p.getSupportedFormat().equalsIgnoreCase(format))
                .findFirst();
    }

    /**
     * 解析并注入文档；调用方必须提供已认证的 actor，以便所有分片和向量操作 绑定到同一租户。
     *
     * @param actor 已认证的租户和用户上下文
     * @param documentId 文档 ID
     * @param content 文本内容（已解析）
     * @param knowledgeIds 关联知识点 ID
     */
    public List<KnowledgeChunkBO> ingest(
            ActorContext actor,
            Long spaceId,
            Long documentId,
            Long versionId,
            String content,
            List<Long> knowledgeIds) {
        if (actor == null) {
            throw new IllegalArgumentException("actor is required");
        }
        return ingestInternal(actor, spaceId, documentId, versionId, content, knowledgeIds);
    }

    private List<KnowledgeChunkBO> ingestInternal(
            ActorContext actor,
            Long spaceId,
            Long documentId,
            Long versionId,
            String content,
            List<Long> knowledgeIds) {
        TenantId ownerTenant = actor.tenantId();
        if (enterpriseRepository != null) {
            KnowledgeDocumentVersionBO version =
                    enterpriseRepository.findVersion(ownerTenant, versionId);
            if (version == null || !documentId.equals(version.getDocumentId())) {
                throw new IllegalArgumentException("document version does not belong to document");
            }
        }
        List<KnowledgeChunkBO> chunkDOs = buildChunks(
                actor, spaceId, documentId, versionId, content, knowledgeIds);
        if (!chunkRepository.replaceByDocumentVersion(ownerTenant, documentId, versionId, chunkDOs)) {
            return List.of();
        }
        log.info(
                "文档 {} 注入完成: {} chunks → H2（发布时通过 VectorStoreProvider 构建版本索引）",
                documentId,
                chunkDOs.size());
        return chunkDOs;
    }

    /** 先暂存所有分块并生成嵌入，再发布该不可变文档版本。 */
    private List<KnowledgeChunkBO> buildChunks(
            ActorContext actor,
            Long spaceId,
            Long documentId,
            Long versionId,
            String content,
            List<Long> knowledgeIds) {
        TenantId ownerTenant = actor.tenantId();
        List<Chunk> chunks = chunkSplitter.split(content);
        log.info("文档 {} 切分为 {} 个 Chunk", documentId, chunks.size());

        List<KnowledgeChunkBO> chunkDOs = new ArrayList<>();

        for (Chunk chunk : chunks) {
            float[] vector = embeddingService.embed(actor, chunk.content());

            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("documentId", documentId);
            meta.put("tenantId", ownerTenant.value());
            if (spaceId != null) {
                meta.put("spaceId", spaceId);
            }
            meta.put("chunkIndex", chunk.index());
            meta.put("startPos", chunk.startPos());
            meta.put("endPos", chunk.endPos());
            if (knowledgeIds != null && !knowledgeIds.isEmpty()) {
                meta.put("knowledgeId", String.valueOf(knowledgeIds.get(0)));
                meta.put(
                        "knowledgeIds",
                        knowledgeIds.stream()
                                .map(String::valueOf)
                                .collect(java.util.stream.Collectors.joining(",")));
            }
            KnowledgeChunkBO chunkDO = new KnowledgeChunkBO();
            chunkDO.setDocumentId(documentId);
            chunkDO.setTenantId(ownerTenant.value());
            chunkDO.setSpaceId(spaceId);
            chunkDO.setVersionId(versionId);
            chunkDO.setContent(chunk.content());
            chunkDO.setEmbeddingBinary(toBytes(vector));
            chunkDO.setEmbeddingModel("default");
            chunkDO.setEmbeddingDimension(vector.length);
            chunkDO.setMetadata(JSONUtils.toJsonString(meta));
            chunkDO.setChunkIndex(chunk.index());
            chunkDO.setStartOffset(chunk.startPos());
            chunkDO.setEndOffset(chunk.endPos());
            chunkDO.setTokenCount(Math.max(1, chunk.content().length() / 2));
            chunkDO.setStatus(1);
            chunkDO.setDelFlag(0);
            chunkDOs.add(chunkDO);
        }
        return chunkDOs;
    }

    /** 查询指定文档版本是否存在可用分块。 */
    public boolean hasChunks(TenantId tenantId, Long documentId, Long versionId) {
        return !chunkRepository.findByDocumentVersion(tenantId, documentId, versionId).isEmpty();
    }

    /** 将已有嵌入的分块复制到新建的不可变文档版本。 */
    public boolean copyVersionChunks(
            TenantId tenantId, Long documentId, Long sourceVersionId, Long targetVersionId) {
        return chunkRepository.copyByDocumentVersion(
                tenantId, documentId, sourceVersionId, targetVersionId);
    }

    /** 在租约仍有效时，原子发布暂存分块并更新文档和任务状态。 */
    public boolean ingestIfClaimed(
            ActorContext actor,
            Long spaceId,
            KnowledgeDocumentBO document,
            KnowledgeDocumentVersionBO version,
            KnowledgeIngestionJobBO job,
            String content,
            String title,
            List<Long> knowledgeIds) {
        if (actor == null) throw new IllegalArgumentException("actor is required");
        List<KnowledgeChunkBO> chunks =
                buildChunks(
                        actor,
                        spaceId,
                        job.getDocumentId(),
                        job.getVersionId(),
                        content,
                        knowledgeIds);
        return publishClaimedIngestion(actor.tenantId(), job, version, document, title, chunks);
    }

    public boolean publishClaimedIngestion(
            TenantId tenantId,
            KnowledgeIngestionJobBO job,
            KnowledgeDocumentVersionBO version,
            KnowledgeDocumentBO document,
            String title,
            List<KnowledgeChunkBO> chunks) {
        if (transactionTemplate == null) {
            throw new IllegalStateException("agent transaction manager is required for ingestion publication");
        }
        Boolean published =
                transactionTemplate.execute(
                        status -> {
                            if (!chunkRepository.replaceByDocumentVersionIfClaimed(
                                    tenantId,
                                    job.getDocumentId(),
                                    job.getVersionId(),
                                    job.getId(),
                                    job.getLockVersion(),
                                    chunks)) {
                                return false;
                            }
                            if (!enterpriseRepository.completeClaimedIngestion(
                                    tenantId, job, version, document, title)) {
                                status.setRollbackOnly();
                                return false;
                            }
                            return true;
                        });
        return Boolean.TRUE.equals(published);
    }

    /**
     * 删除或移除 文档 Ingestion 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param documentId 用于定位document的标识。
     */
    public void delete(TenantId tenantId, Long documentId) {
        chunkRepository.deleteByDocumentId(tenantId, documentId);
    }

    private byte[] toBytes(float[] vector) {
        ByteBuffer buffer =
                ByteBuffer.allocate(vector.length * Float.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (float value : vector) {
            buffer.putFloat(value);
        }
        return buffer.array();
    }
}
