package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeRelationBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeRelationRepository;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeRelationDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeRelationMapper;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 负责 知识 关系 的持久化查询、保存和删除，并维护数据访问边界。
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
     * 查询 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param targetId 用于定位target的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param sourceId 用于定位source的标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param targetId 用于定位target的标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 创建或保存 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 知识 关系 相关操作生成的结果数据。
     */
    @Override
    public int insert(TenantId tenantId, KnowledgeRelationBO bo) {
        bo.setTenantId(tenantId.value());
        KnowledgeRelationDO data = MapstructUtils.convert(bo, KnowledgeRelationDO.class);
        data.setTenantId(tenantId.value());
        return relationMapper.insert(data);
    }

    /**
     * 删除或移除 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param sourceId 用于定位source的标识。
     * @param targetId 用于定位target的标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @return 返回 知识 关系 相关操作生成的结果数据。
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
     * 删除或移除 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回 知识 关系 相关操作生成的结果数据。
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
     * 查询 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<KnowledgeRelationBO> findBySpace(TenantId tenantId, Long spaceId) {
        return convert(
                relationMapper.selectListByQuery(
                        base(tenantId).eq(KnowledgeRelationDO::getSpaceId, spaceId)));
    }

    /**
     * 执行 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param sourceId 用于定位source的标识。
     * @param targetId 用于定位target的标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @return 返回本次条件判断是否成立。
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
     * 更新或设置 知识 关系 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
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
