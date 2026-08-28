package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.knowledge.domain.model.KnowledgeChunkBO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeChunkDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeChunkMapper;
import com.shiyu.ai.kernel.context.TenantId;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class KnowledgeChunkRepositoryImpl implements com.shiyu.ai.knowledge.port.repository.KnowledgeChunkRepository {

    private final KnowledgeChunkMapper mapper;

    public KnowledgeChunkRepositoryImpl(KnowledgeChunkMapper mapper) {
        this.mapper = mapper;
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
        KnowledgeChunkDO d = mapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeChunkDO::getTenantId, tenantId.value())
                .eq(KnowledgeChunkDO::getId, id)
                .eq(KnowledgeChunkDO::getDelFlag, 0));
        return d != null ? MapstructUtils.convert(d, KnowledgeChunkBO.class) : null;
    }

    @Override
    public void deleteByDocumentId(TenantId tenantId, Long documentId) {
        requireTenant(tenantId);
        mapper.deleteByQuery(QueryWrapper.create()
                .eq(KnowledgeChunkDO::getTenantId, tenantId.value())
                .eq(KnowledgeChunkDO::getDocumentId, documentId));
    }

    public List<KnowledgeChunkBO> findBySpace(TenantId tenantId, Long spaceId) {
        requireTenant(tenantId);
        return MapstructUtils.convert(mapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeChunkDO::getTenantId, tenantId.value())
                        .eq(KnowledgeChunkDO::getSpaceId, spaceId)
                        .eq(KnowledgeChunkDO::getDelFlag, 0)
                        .orderBy(KnowledgeChunkDO::getDocumentId, true)
                        .orderBy(KnowledgeChunkDO::getChunkIndex, true)), KnowledgeChunkBO.class);
    }

    public void assignDefaultSpace(TenantId tenantId, Long spaceId) {
        requireTenant(tenantId);
        List<KnowledgeChunkDO> records = mapper.selectListByQuery(
                QueryWrapper.create().eq(KnowledgeChunkDO::getTenantId, tenantId.value())
                        .isNull(KnowledgeChunkDO::getSpaceId));
        for (KnowledgeChunkDO record : records) {
            record.setSpaceId(spaceId);
            mapper.updateByQuery(record, QueryWrapper.create()
                    .eq(KnowledgeChunkDO::getTenantId, tenantId.value())
                    .eq(KnowledgeChunkDO::getId, record.getId()));
        }
    }

    private static void requireTenant(TenantId tenantId) {
        if (tenantId == null || tenantId.value() <= 0) {
            throw new IllegalArgumentException("tenantId is required");
        }
    }
}

