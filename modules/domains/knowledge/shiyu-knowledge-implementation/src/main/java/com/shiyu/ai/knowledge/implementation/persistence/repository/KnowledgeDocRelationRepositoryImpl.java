package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocRelationBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeDocRelationRepository;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocRelationDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDocRelationMapper;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@code KnowledgeDocRelationRepositoryImpl} 实现知识模块的持久化端口，负责在领域对象与存储模型之间转换。
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
     * {@code deleteByKnowledgeId} 释放或移除当前操作涉及的资源。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    public void deleteByKnowledgeId(TenantId tenantId, Long spaceId, Long id) {
        mapper.deleteByQuery(
                base(tenantId)
                        .eq(KnowledgeDocRelationDO::getSpaceId, spaceId)
                        .eq(KnowledgeDocRelationDO::getKnowledgeId, id));
    }

    /**
     * {@code selectByDocId} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code selectByKnowledgeId} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code deleteByDocId} 释放或移除当前操作涉及的资源。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    public void deleteByDocId(TenantId tenantId, Long spaceId, Long id) {
        mapper.deleteByQuery(
                base(tenantId)
                        .eq(KnowledgeDocRelationDO::getSpaceId, spaceId)
                        .eq(KnowledgeDocRelationDO::getDocId, id));
    }

    /**
     * {@code assignDefaultSpace} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
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
