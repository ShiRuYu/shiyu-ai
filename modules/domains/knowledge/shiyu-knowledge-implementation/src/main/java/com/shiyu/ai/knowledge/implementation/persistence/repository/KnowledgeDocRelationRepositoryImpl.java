package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocRelationBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeDocRelationRepository;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocRelationDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDocRelationMapper;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 负责 知识 Doc 关系 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class KnowledgeDocRelationRepositoryImpl implements KnowledgeDocRelationRepository {
    /**
     * 映射器，表示当前对象中的对应属性。
     */
    @Resource private KnowledgeDocRelationMapper mapper;

    @Override
    public void insertBatch(TenantId tenantId, List<KnowledgeDocRelationBO> relations) {
        relations.forEach(relation -> relation.setTenantId(tenantId.value()));
        mapper.insertBatch(MapstructUtils.convert(relations, KnowledgeDocRelationDO.class));
    }

    /**
     * 删除或移除 知识 Doc 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param id 用于定位目标业务对象的标识。
     */
    @Override
    public void deleteByKnowledgeId(TenantId tenantId, Long spaceId, Long id) {
        mapper.deleteByQuery(
                base(tenantId)
                        .eq(KnowledgeDocRelationDO::getSpaceId, spaceId)
                        .eq(KnowledgeDocRelationDO::getKnowledgeId, id));
    }

    /**
     * 查询 知识 Doc 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<KnowledgeDocRelationBO> selectByDocId(TenantId tenantId, Long spaceId, Long id) {
        return convert(
                mapper.selectListByQuery(
                        base(tenantId)
                                .eq(KnowledgeDocRelationDO::getSpaceId, spaceId)
                                .eq(KnowledgeDocRelationDO::getDocId, id)));
    }

    /**
     * 查询 知识 Doc 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<KnowledgeDocRelationBO> selectByKnowledgeId(
            TenantId tenantId, Long spaceId, Long id) {
        return convert(
                mapper.selectListByQuery(
                        base(tenantId)
                                .eq(KnowledgeDocRelationDO::getSpaceId, spaceId)
                                .eq(KnowledgeDocRelationDO::getKnowledgeId, id)));
    }

    /**
     * 删除或移除 知识 Doc 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param id 用于定位目标业务对象的标识。
     */
    @Override
    public void deleteByDocId(TenantId tenantId, Long spaceId, Long id) {
        mapper.deleteByQuery(
                base(tenantId)
                        .eq(KnowledgeDocRelationDO::getSpaceId, spaceId)
                        .eq(KnowledgeDocRelationDO::getDocId, id));
    }

    /**
     * 更新或设置 知识 Doc 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     */
    @Override
    public void assignDefaultSpace(TenantId tenantId, Long spaceId) {
        List<KnowledgeDocRelationDO> records =
                mapper.selectListByQuery(base(tenantId).isNull(KnowledgeDocRelationDO::getSpaceId));
        for (KnowledgeDocRelationDO record : records) {
            record.setSpaceId(spaceId);
            mapper.update(record);
        }
    }

    private QueryWrapper base(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        return QueryWrapper.create()
                .eq(KnowledgeDocRelationDO::getTenantId, tenantId.value())
                .eq(KnowledgeDocRelationDO::getDelFlag, 0);
    }

    private List<KnowledgeDocRelationBO> convert(List<KnowledgeDocRelationDO> records) {
        return MapstructUtils.convert(records, KnowledgeDocRelationBO.class);
    }
}
