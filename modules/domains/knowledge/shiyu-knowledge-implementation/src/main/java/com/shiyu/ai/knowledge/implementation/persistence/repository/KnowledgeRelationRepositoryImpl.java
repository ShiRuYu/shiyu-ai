package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeRelationBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeRelationRepository;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeRelationDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeRelationMapper;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@code KnowledgeRelationRepositoryImpl} 实现知识模块的持久化端口，负责在领域对象与存储模型之间转换。
 */
@Component
public class KnowledgeRelationRepositoryImpl implements KnowledgeRelationRepository {
    /**
     * relationMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private KnowledgeRelationMapper relationMapper;

    @Override
    public List<KnowledgeRelationBO> findBySourceId(
            TenantId tenantId, Long spaceId, Long sourceId) {
        return convert(
                relationMapper.selectListByQuery(
                        base(tenantId)
                                .eq(KnowledgeRelationDO::getSpaceId, spaceId)
                                .eq(KnowledgeRelationDO::getSourceId, sourceId)));
    }

    /**
     * {@code findByTargetId} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param targetId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<KnowledgeRelationBO> findByTargetId(
            TenantId tenantId, Long spaceId, Long targetId) {
        return convert(
                relationMapper.selectListByQuery(
                        base(tenantId)
                                .eq(KnowledgeRelationDO::getSpaceId, spaceId)
                                .eq(KnowledgeRelationDO::getTargetId, targetId)));
    }

    /**
     * {@code findBySourceIdAndType} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param sourceId 参数值，用于执行当前操作。
     * @param type 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<KnowledgeRelationBO> findBySourceIdAndType(
            TenantId tenantId, Long spaceId, Long sourceId, String type) {
        return convert(
                relationMapper.selectListByQuery(
                        base(tenantId)
                                .eq(KnowledgeRelationDO::getSpaceId, spaceId)
                                .eq(KnowledgeRelationDO::getSourceId, sourceId)
                                .eq(KnowledgeRelationDO::getRelationType, type)));
    }

    /**
     * {@code findByTargetIdAndType} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param targetId 参数值，用于执行当前操作。
     * @param type 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<KnowledgeRelationBO> findByTargetIdAndType(
            TenantId tenantId, Long spaceId, Long targetId, String type) {
        return convert(
                relationMapper.selectListByQuery(
                        base(tenantId)
                                .eq(KnowledgeRelationDO::getSpaceId, spaceId)
                                .eq(KnowledgeRelationDO::getTargetId, targetId)
                                .eq(KnowledgeRelationDO::getRelationType, type)));
    }

    /**
     * {@code insert} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param bo 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public int insert(TenantId tenantId, KnowledgeRelationBO bo) {
        bo.setTenantId(tenantId.value());
        KnowledgeRelationDO data = MapstructUtils.convert(bo, KnowledgeRelationDO.class);
        data.setTenantId(tenantId.value());
        return relationMapper.insert(data);
    }

    /**
     * {@code deleteBySourceAndTargetAndType} 释放或移除当前操作涉及的资源。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param sourceId 参数值，用于执行当前操作。
     * @param targetId 参数值，用于执行当前操作。
     * @param type 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public int deleteBySourceAndTargetAndType(
            TenantId tenantId, Long spaceId, Long sourceId, Long targetId, String type) {
        return relationMapper.deleteByQuery(
                base(tenantId)
                        .eq(KnowledgeRelationDO::getSpaceId, spaceId)
                        .eq(KnowledgeRelationDO::getSourceId, sourceId)
                        .eq(KnowledgeRelationDO::getTargetId, targetId)
                        .eq(KnowledgeRelationDO::getRelationType, type));
    }

    /**
     * {@code deleteBySourceIdOrTargetId} 释放或移除当前操作涉及的资源。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public int deleteBySourceIdOrTargetId(TenantId tenantId, Long spaceId, Long knowledgeId) {
        int count =
                relationMapper.deleteByQuery(
                        base(tenantId)
                                .eq(KnowledgeRelationDO::getSpaceId, spaceId)
                                .eq(KnowledgeRelationDO::getSourceId, knowledgeId));
        count +=
                relationMapper.deleteByQuery(
                        base(tenantId)
                                .eq(KnowledgeRelationDO::getSpaceId, spaceId)
                                .eq(KnowledgeRelationDO::getTargetId, knowledgeId));
        return count;
    }

    /**
     * {@code findBySpace} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<KnowledgeRelationBO> findBySpace(TenantId tenantId, Long spaceId) {
        return convert(
                relationMapper.selectListByQuery(
                        base(tenantId).eq(KnowledgeRelationDO::getSpaceId, spaceId)));
    }

    /**
     * {@code exists} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param sourceId 参数值，用于执行当前操作。
     * @param targetId 参数值，用于执行当前操作。
     * @param type 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean exists(
            TenantId tenantId, Long spaceId, Long sourceId, Long targetId, String type) {
        return relationMapper.selectCountByQuery(
                        base(tenantId)
                                .eq(KnowledgeRelationDO::getSpaceId, spaceId)
                                .eq(KnowledgeRelationDO::getSourceId, sourceId)
                                .eq(KnowledgeRelationDO::getTargetId, targetId)
                                .eq(KnowledgeRelationDO::getRelationType, type))
                > 0;
    }

    /**
     * {@code assignDefaultSpace} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     */
    @Override
    public void assignDefaultSpace(TenantId tenantId, Long spaceId) {
        List<KnowledgeRelationDO> records =
                relationMapper.selectListByQuery(
                        base(tenantId).isNull(KnowledgeRelationDO::getSpaceId));
        for (KnowledgeRelationDO record : records) {
            record.setSpaceId(spaceId);
            relationMapper.update(record);
        }
    }

    private QueryWrapper base(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        return QueryWrapper.create()
                .eq(KnowledgeRelationDO::getTenantId, tenantId.value())
                .eq(KnowledgeRelationDO::getDelFlag, 0);
    }

    private List<KnowledgeRelationBO> convert(List<KnowledgeRelationDO> records) {
        return MapstructUtils.convert(records, KnowledgeRelationBO.class);
    }
}
