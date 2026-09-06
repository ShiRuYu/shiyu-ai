package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.domain.model.KnowledgeRelationBO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeRelationDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeRelationMapper;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRelationRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeRelationRepositoryImpl implements KnowledgeRelationRepository {
    @Resource
    private KnowledgeRelationMapper relationMapper;

    @Override
    public List<KnowledgeRelationBO> findBySourceId(TenantId tenantId, Long spaceId, Long sourceId) {
        return convert(relationMapper.selectListByQuery(base(tenantId).eq(KnowledgeRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeRelationDO::getSourceId, sourceId)));
    }

    @Override
    public List<KnowledgeRelationBO> findByTargetId(TenantId tenantId, Long spaceId, Long targetId) {
        return convert(relationMapper.selectListByQuery(base(tenantId).eq(KnowledgeRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeRelationDO::getTargetId, targetId)));
    }

    @Override
    public List<KnowledgeRelationBO> findBySourceIdAndType(TenantId tenantId, Long spaceId, Long sourceId, String type) {
        return convert(relationMapper.selectListByQuery(base(tenantId).eq(KnowledgeRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeRelationDO::getSourceId, sourceId)
                .eq(KnowledgeRelationDO::getRelationType, type)));
    }

    @Override
    public List<KnowledgeRelationBO> findByTargetIdAndType(TenantId tenantId, Long spaceId, Long targetId, String type) {
        return convert(relationMapper.selectListByQuery(base(tenantId).eq(KnowledgeRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeRelationDO::getTargetId, targetId)
                .eq(KnowledgeRelationDO::getRelationType, type)));
    }

    @Override
    public int insert(TenantId tenantId, KnowledgeRelationBO bo) {
        bo.setTenantId(tenantId.value());
        KnowledgeRelationDO data = MapstructUtils.convert(bo, KnowledgeRelationDO.class);
        data.setTenantId(tenantId.value());
        return relationMapper.insert(data);
    }

    @Override
    public int deleteBySourceAndTargetAndType(TenantId tenantId, Long spaceId, Long sourceId, Long targetId, String type) {
        return relationMapper.deleteByQuery(base(tenantId).eq(KnowledgeRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeRelationDO::getSourceId, sourceId)
                .eq(KnowledgeRelationDO::getTargetId, targetId)
                .eq(KnowledgeRelationDO::getRelationType, type));
    }

    @Override
    public int deleteBySourceIdOrTargetId(TenantId tenantId, Long spaceId, Long knowledgeId) {
        int count = relationMapper.deleteByQuery(base(tenantId).eq(KnowledgeRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeRelationDO::getSourceId, knowledgeId));
        count += relationMapper.deleteByQuery(base(tenantId).eq(KnowledgeRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeRelationDO::getTargetId, knowledgeId));
        return count;
    }

    @Override
    public List<KnowledgeRelationBO> findBySpace(TenantId tenantId, Long spaceId) {
        return convert(relationMapper.selectListByQuery(base(tenantId).eq(KnowledgeRelationDO::getSpaceId, spaceId)));
    }

    @Override
    public boolean exists(TenantId tenantId, Long spaceId, Long sourceId, Long targetId, String type) {
        return relationMapper.selectCountByQuery(base(tenantId).eq(KnowledgeRelationDO::getSpaceId, spaceId)
                .eq(KnowledgeRelationDO::getSourceId, sourceId)
                .eq(KnowledgeRelationDO::getTargetId, targetId)
                .eq(KnowledgeRelationDO::getRelationType, type)) > 0;
    }

    @Override
    public void assignDefaultSpace(TenantId tenantId, Long spaceId) {
        List<KnowledgeRelationDO> records = relationMapper.selectListByQuery(
                base(tenantId).isNull(KnowledgeRelationDO::getSpaceId));
        for (KnowledgeRelationDO record : records) {
            record.setSpaceId(spaceId);
            relationMapper.update(record);
        }
    }

    private QueryWrapper base(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        return QueryWrapper.create().eq(KnowledgeRelationDO::getTenantId, tenantId.value())
                .eq(KnowledgeRelationDO::getDelFlag, 0);
    }

    private List<KnowledgeRelationBO> convert(List<KnowledgeRelationDO> records) {
        return MapstructUtils.convert(records, KnowledgeRelationBO.class);
    }
}
