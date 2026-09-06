package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocRelationBO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocRelationDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDocRelationMapper;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocRelationRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeDocRelationRepositoryImpl implements KnowledgeDocRelationRepository {
    @Resource
    private KnowledgeDocRelationMapper mapper;

    @Override
    public void insertBatch(TenantId tenantId, List<KnowledgeDocRelationBO> relations) {
        relations.forEach(relation -> relation.setTenantId(tenantId.value()));
        mapper.insertBatch(MapstructUtils.convert(relations, KnowledgeDocRelationDO.class));
    }

    @Override
    public void deleteByKnowledgeId(TenantId tenantId, Long spaceId, Long id) {
        mapper.deleteByQuery(base(tenantId).eq(KnowledgeDocRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeDocRelationDO::getKnowledgeId, id));
    }

    @Override
    public List<KnowledgeDocRelationBO> selectByDocId(TenantId tenantId, Long spaceId, Long id) {
        return convert(mapper.selectListByQuery(base(tenantId).eq(KnowledgeDocRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeDocRelationDO::getDocId, id)));
    }

    @Override
    public List<KnowledgeDocRelationBO> selectByKnowledgeId(TenantId tenantId, Long spaceId, Long id) {
        return convert(mapper.selectListByQuery(base(tenantId).eq(KnowledgeDocRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeDocRelationDO::getKnowledgeId, id)));
    }

    @Override
    public void deleteByDocId(TenantId tenantId, Long spaceId, Long id) {
        mapper.deleteByQuery(base(tenantId).eq(KnowledgeDocRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeDocRelationDO::getDocId, id));
    }

    @Override
    public void assignDefaultSpace(TenantId tenantId, Long spaceId) {
        List<KnowledgeDocRelationDO> records = mapper.selectListByQuery(
                base(tenantId).isNull(KnowledgeDocRelationDO::getSpaceId));
        for (KnowledgeDocRelationDO record : records) {
            record.setSpaceId(spaceId);
            mapper.update(record);
        }
    }

    private QueryWrapper base(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        return QueryWrapper.create().eq(KnowledgeDocRelationDO::getTenantId, tenantId.value())
                .eq(KnowledgeDocRelationDO::getDelFlag, 0);
    }

    private List<KnowledgeDocRelationBO> convert(List<KnowledgeDocRelationDO> records) {
        return MapstructUtils.convert(records, KnowledgeDocRelationBO.class);
    }
}
