package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeMapper;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 负责 知识 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class KnowledgeRepositoryImpl implements KnowledgeRepository {
    /**
     * knowledgeMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private KnowledgeMapper knowledgeMapper;

    @Override
    public KnowledgeBO findById(TenantId tenantId, Long id) {
        return convert(knowledgeMapper.selectOneByQuery(base(tenantId).eq(KnowledgeDO::getId, id)));
    }

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    @Override
    public KnowledgeBO findByCode(TenantId tenantId, String code) {
        return convert(
                knowledgeMapper.selectOneByQuery(base(tenantId).eq(KnowledgeDO::getCode, code)));
    }

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<KnowledgeBO> findAll(TenantId tenantId) {
        return convertList(
                knowledgeMapper.selectListByQuery(base(tenantId).eq(KnowledgeDO::getStatus, 1)));
    }

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<KnowledgeBO> searchByName(TenantId tenantId, String keyword, int topK) {
        return convertList(
                knowledgeMapper.selectListByQuery(
                        base(tenantId)
                                .eq(KnowledgeDO::getStatus, 1)
                                .like(KnowledgeDO::getName, keyword)
                                .orderBy(KnowledgeDO::getId, true)
                                .limit(0, topK)));
    }

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param offset 用于完成本次业务处理的 offset 参数。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<KnowledgeBO> page(TenantId tenantId, int offset, int limit) {
        return page(tenantId, offset, limit, null, null);
    }

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param offset 用于完成本次业务处理的 offset 参数。
     * @param limit 每页返回的数据数量。
     * @param category 用于完成本次业务处理的 category 参数。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<KnowledgeBO> page(
            TenantId tenantId, int offset, int limit, String category, String keyword) {
        QueryWrapper query = base(tenantId).eq(KnowledgeDO::getStatus, 1);
        if (category != null && !category.isBlank()) query.eq(KnowledgeDO::getCategory, category);
        if (keyword != null && !keyword.isBlank()) query.like(KnowledgeDO::getName, keyword);
        return convertList(
                knowledgeMapper.selectListByQuery(
                        query.orderBy(KnowledgeDO::getId, true).limit(offset, limit)));
    }

    /**
     * 执行 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    @Override
    public long count(TenantId tenantId) {
        return count(tenantId, null, null);
    }

    /**
     * 执行 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param category 用于完成本次业务处理的 category 参数。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    @Override
    public long count(TenantId tenantId, String category, String keyword) {
        QueryWrapper query = base(tenantId).eq(KnowledgeDO::getStatus, 1);
        if (category != null && !category.isBlank()) query.eq(KnowledgeDO::getCategory, category);
        if (keyword != null && !keyword.isBlank()) query.like(KnowledgeDO::getName, keyword);
        return knowledgeMapper.selectCountByQuery(query);
    }

    /**
     * 创建或保存 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    @Override
    public int insert(TenantId tenantId, KnowledgeBO bo) {
        bo.setTenantId(tenantId.value());
        KnowledgeDO data = MapstructUtils.convert(bo, KnowledgeDO.class);
        data.setTenantId(tenantId.value());
        int rows = knowledgeMapper.insert(data);
        bo.setId(data.getId());
        return rows;
    }

    /**
     * 更新或设置 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    @Override
    public int update(TenantId tenantId, KnowledgeBO bo) {
        requireTenant(tenantId, bo.getTenantId());
        QueryWrapper scope = base(tenantId).eq(KnowledgeDO::getId, bo.getId());
        if (knowledgeMapper.selectOneByQuery(scope) == null) return 0;
        KnowledgeDO data = MapstructUtils.convert(bo, KnowledgeDO.class);
        data.setTenantId(tenantId.value());
        return knowledgeMapper.updateByQuery(data, scope);
    }

    /**
     * 删除或移除 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    @Override
    public int deleteById(TenantId tenantId, Long id) {
        return knowledgeMapper.deleteByQuery(base(tenantId).eq(KnowledgeDO::getId, id));
    }

    /**
     * 执行 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean existsByCode(TenantId tenantId, String code) {
        return findByCode(tenantId, code) != null;
    }

    /**
     * 执行 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean existsBySpaceAndCode(TenantId tenantId, Long spaceId, String code) {
        return knowledgeMapper.selectCountByQuery(
                        base(tenantId)
                                .eq(KnowledgeDO::getSpaceId, spaceId)
                                .eq(KnowledgeDO::getCode, code))
                > 0;
    }

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<KnowledgeBO> findBySpace(TenantId tenantId, Long spaceId) {
        return convertList(
                knowledgeMapper.selectListByQuery(
                        base(tenantId)
                                .eq(KnowledgeDO::getSpaceId, spaceId)
                                .eq(KnowledgeDO::getStatus, 1)));
    }

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param category 用于完成本次业务处理的 category 参数。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    @Override
    public PageData<KnowledgeBO> pageBySpace(
            TenantId tenantId,
            Long spaceId,
            int pageNum,
            int pageSize,
            String keyword,
            String category) {
        QueryWrapper query =
                base(tenantId).eq(KnowledgeDO::getSpaceId, spaceId).eq(KnowledgeDO::getStatus, 1);
        if (keyword != null && !keyword.isBlank()) query.like(KnowledgeDO::getName, keyword);
        if (category != null && !category.isBlank()) query.eq(KnowledgeDO::getCategory, category);
        var page =
                knowledgeMapper.paginate(
                        pageNum, pageSize, query.orderBy(KnowledgeDO::getId, false));
        return new PageData<>(convertList(page.getRecords()), page.getTotalRow());
    }

    /**
     * 删除或移除 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @param spaceId 用于定位space的标识。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    @Override
    public int deleteByIdAndSpace(TenantId tenantId, Long id, Long spaceId) {
        return knowledgeMapper.deleteByQuery(
                base(tenantId).eq(KnowledgeDO::getId, id).eq(KnowledgeDO::getSpaceId, spaceId));
    }

    /**
     * 更新或设置 知识 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     */
    @Override
    public void assignDefaultSpace(TenantId tenantId, Long spaceId) {
        List<KnowledgeDO> records =
                knowledgeMapper.selectListByQuery(base(tenantId).isNull(KnowledgeDO::getSpaceId));
        for (KnowledgeDO record : records) {
            record.setSpaceId(spaceId);
            knowledgeMapper.update(record);
        }
    }

    private QueryWrapper base(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        TenantScope.requireMatches(tenantId);
        return QueryWrapper.create()
                .eq(KnowledgeDO::getTenantId, tenantId.value())
                .eq(KnowledgeDO::getDelFlag, 0);
    }

    private void requireTenant(TenantId tenantId, Long resourceTenantId) {
        TenantScope.requireMatches(tenantId);
        if (resourceTenantId == null || tenantId.value() != resourceTenantId) {
            throw new IllegalArgumentException("knowledge tenant does not match actor tenant");
        }
    }

    private KnowledgeBO convert(KnowledgeDO data) {
        return data == null ? null : MapstructUtils.convert(data, KnowledgeBO.class);
    }

    private List<KnowledgeBO> convertList(List<KnowledgeDO> data) {
        return MapstructUtils.convert(data, KnowledgeBO.class);
    }
}
