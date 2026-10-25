package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeChunkRepository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeChunkBO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeChunkDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeChunkMapper;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import javax.sql.DataSource;
import java.util.List;

/**
 * 负责 知识 Chunk 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Repository
public class KnowledgeChunkRepositoryImpl
        implements com.shiyu.ai.knowledge.implementation.domain.port.repository
                .KnowledgeChunkRepository {

    /**
     * 映射器，表示当前对象中的对应属性。
     */
    private final KnowledgeChunkMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    @org.springframework.beans.factory.annotation.Autowired
    public KnowledgeChunkRepositoryImpl(
            KnowledgeChunkMapper mapper, @Qualifier("agentDataSource") DataSource dataSource) {
        this.mapper = mapper;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public KnowledgeChunkRepositoryImpl(KnowledgeChunkMapper mapper) {
        this.mapper = mapper;
        this.jdbcTemplate = null;
    }

    public void insert(TenantId tenantId, KnowledgeChunkBO bo) {
        requireTenant(tenantId);
        bo.setTenantId(tenantId.value());
        KnowledgeChunkDO dataObject = MapstructUtils.convert(bo, KnowledgeChunkDO.class);
        dataObject.setTenantId(tenantId.value());
        mapper.insert(dataObject);
        bo.setId(dataObject.getId());
    }

    public KnowledgeChunkBO getById(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        KnowledgeChunkDO d =
                mapper.selectOneByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeChunkDO::getTenantId, tenantId.value())
                                .eq(KnowledgeChunkDO::getId, id)
                                .eq(KnowledgeChunkDO::getDelFlag, 0));
        return d != null ? MapstructUtils.convert(d, KnowledgeChunkBO.class) : null;
    }

    @Override
    public void deleteByDocumentId(TenantId tenantId, Long documentId) {
        requireTenant(tenantId);
        mapper.deleteByQuery(
                QueryWrapper.create()
                        .eq(KnowledgeChunkDO::getTenantId, tenantId.value())
                        .eq(KnowledgeChunkDO::getDocumentId, documentId));
    }

    @Override
    public List<KnowledgeChunkBO> findByDocumentVersion(
            TenantId tenantId, Long documentId, Long versionId) {
        requireTenant(tenantId);
        return MapstructUtils.convert(
                mapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeChunkDO::getTenantId, tenantId.value())
                                .eq(KnowledgeChunkDO::getDocumentId, documentId)
                                .eq(KnowledgeChunkDO::getVersionId, versionId)
                                .eq(KnowledgeChunkDO::getDelFlag, 0)
                                .orderBy(KnowledgeChunkDO::getChunkIndex, true)),
                KnowledgeChunkBO.class);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean replaceByDocumentVersion(
            TenantId tenantId, Long documentId, Long versionId, List<KnowledgeChunkBO> chunks) {
        requireTenant(tenantId);
        if (documentId == null || versionId == null || chunks == null) {
            throw new IllegalArgumentException("documentId, versionId and chunks are required");
        }
        validateChunks(documentId, versionId, chunks);
        if (!documentMapperVersionIsCurrent(tenantId, documentId, versionId)) return false;
        deleteVersionChunks(tenantId, documentId, versionId);
        insertChunks(tenantId, chunks);
        return true;
    }

    private boolean documentMapperVersionIsCurrent(
            TenantId tenantId, Long documentId, Long versionId) {
        Long current =
                jdbcTemplate.query(
                        "SELECT current_version_id FROM knowledge_document"
                                + " WHERE tenant_id = ? AND id = ? FOR UPDATE",
                        resultSet -> resultSet.next() ? resultSet.getLong(1) : null,
                        tenantId.value(), documentId);
        return current != null && current.equals(versionId);
    }

    private void validateChunks(Long documentId, Long versionId, List<KnowledgeChunkBO> chunks) {
        for (KnowledgeChunkBO chunk : chunks) {
            if (!documentId.equals(chunk.getDocumentId()) || !versionId.equals(chunk.getVersionId())) {
                throw new IllegalArgumentException("chunk document/version does not match replacement");
            }
        }
    }

    private void deleteVersionChunks(TenantId tenantId, Long documentId, Long versionId) {
        mapper.deleteByQuery(
                QueryWrapper.create()
                        .eq(KnowledgeChunkDO::getTenantId, tenantId.value())
                        .eq(KnowledgeChunkDO::getDocumentId, documentId)
                        .eq(KnowledgeChunkDO::getVersionId, versionId));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean replaceByDocumentVersionIfClaimed(
            TenantId tenantId,
            Long documentId,
            Long versionId,
            Long jobId,
            Long lockVersion,
            List<KnowledgeChunkBO> chunks) {
        requireTenant(tenantId);
        if (documentId == null || versionId == null || jobId == null || lockVersion == null || chunks == null) {
            throw new IllegalArgumentException("document, version, claim and chunks are required");
        }
        validateChunks(documentId, versionId, chunks);
        if (jdbcTemplate == null) throw new IllegalStateException("agentDataSource is required");
        Integer claim =
                jdbcTemplate.query(
                        "SELECT j.id FROM knowledge_ingestion_job j"
                                + " JOIN knowledge_document d ON d.tenant_id = j.tenant_id"
                                + " AND d.id = j.document_id WHERE j.tenant_id = ? AND j.id = ?"
                                + " AND j.document_id = ? AND j.version_id = ?"
                                + " AND d.current_version_id = ? AND j.job_type = 'INGEST'"
                                + " AND j.job_status = 'RUNNING' AND j.lock_version = ?"
                                + " AND j.del_flag = 0 FOR UPDATE",
                        resultSet -> resultSet.next() ? resultSet.getInt(1) : null,
                        tenantId.value(), jobId, documentId, versionId, versionId, lockVersion);
        if (claim == null) return false;
        deleteVersionChunks(tenantId, documentId, versionId);
        insertChunks(tenantId, chunks);
        return true;
    }

    private void insertChunks(TenantId tenantId, List<KnowledgeChunkBO> chunks) {
        for (KnowledgeChunkBO chunk : chunks) insert(tenantId, chunk);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean copyByDocumentVersion(
            TenantId tenantId, Long documentId, Long sourceVersionId, Long targetVersionId) {
        requireTenant(tenantId);
        if (documentId == null || sourceVersionId == null || targetVersionId == null) {
            throw new IllegalArgumentException("documentId and version ids are required");
        }
        if (targetVersionId.equals(sourceVersionId)) {
            throw new IllegalArgumentException("target version must be newly created");
        }
        List<KnowledgeChunkBO> source = findByDocumentVersion(tenantId, documentId, sourceVersionId);
        List<KnowledgeChunkBO> copies = new java.util.ArrayList<>(source.size());
        for (KnowledgeChunkBO chunk : source) {
            KnowledgeChunkBO copy = new KnowledgeChunkBO();
            copy.setDocumentId(chunk.getDocumentId());
            copy.setSpaceId(chunk.getSpaceId());
            copy.setVersionId(targetVersionId);
            copy.setTenantId(tenantId.value());
            copy.setContent(chunk.getContent());
            copy.setEmbedding(chunk.getEmbedding());
            copy.setEmbeddingBinary(chunk.getEmbeddingBinary());
            copy.setEmbeddingModel(chunk.getEmbeddingModel());
            copy.setEmbeddingDimension(chunk.getEmbeddingDimension());
            copy.setMetadata(chunk.getMetadata());
            copy.setChunkIndex(chunk.getChunkIndex());
            copy.setPageNumber(chunk.getPageNumber());
            copy.setSectionPath(chunk.getSectionPath());
            copy.setStartOffset(chunk.getStartOffset());
            copy.setEndOffset(chunk.getEndOffset());
            copy.setTokenCount(chunk.getTokenCount());
            copy.setStatus(chunk.getStatus());
            copy.setDelFlag(0);
            copies.add(copy);
        }
        insertChunks(tenantId, copies);
        return !source.isEmpty();
    }

    public List<KnowledgeChunkBO> findBySpace(TenantId tenantId, Long spaceId) {
        requireTenant(tenantId);
        return MapstructUtils.convert(
                mapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeChunkDO::getTenantId, tenantId.value())
                                .eq(KnowledgeChunkDO::getSpaceId, spaceId)
                                .eq(KnowledgeChunkDO::getDelFlag, 0)
                                .orderBy(KnowledgeChunkDO::getDocumentId, true)
                                .orderBy(KnowledgeChunkDO::getChunkIndex, true)),
                KnowledgeChunkBO.class);
    }

    @Override
    public List<KnowledgeChunkBO> findByIds(TenantId tenantId, Long spaceId, List<Long> ids) {
        requireTenant(tenantId);
        if (ids == null || ids.isEmpty()) return List.of();
        return MapstructUtils.convert(
                mapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeChunkDO::getTenantId, tenantId.value())
                                .eq(KnowledgeChunkDO::getSpaceId, spaceId)
                                .eq(KnowledgeChunkDO::getDelFlag, 0)
                                .in(KnowledgeChunkDO::getId, ids)),
                KnowledgeChunkBO.class);
    }

    public void assignDefaultSpace(TenantId tenantId, Long spaceId) {
        requireTenant(tenantId);
        List<KnowledgeChunkDO> records =
                mapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeChunkDO::getTenantId, tenantId.value())
                                .isNull(KnowledgeChunkDO::getSpaceId));
        for (KnowledgeChunkDO record : records) {
            record.setSpaceId(spaceId);
            mapper.updateByQuery(
                    record,
                    QueryWrapper.create()
                            .eq(KnowledgeChunkDO::getTenantId, tenantId.value())
                            .eq(KnowledgeChunkDO::getId, record.getId()));
        }
    }

    private static void requireTenant(TenantId tenantId) {
        if (tenantId == null || tenantId.value() <= 0) {
            throw new IllegalArgumentException("tenantId is required");
        }
        TenantScope.requireMatches(tenantId);
    }
}
