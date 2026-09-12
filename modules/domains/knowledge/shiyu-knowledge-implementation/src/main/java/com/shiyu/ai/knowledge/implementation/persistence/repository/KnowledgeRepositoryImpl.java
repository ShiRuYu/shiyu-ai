package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeMapper;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@code KnowledgeRepositoryImpl} 实现知识模块的持久化端口，负责在领域对象与存储模型之间转换。
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
     * {@code findByCode} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public KnowledgeBO findByCode(TenantId tenantId, String code) {
        return convert(
                knowledgeMapper.selectOneByQuery(base(tenantId).eq(KnowledgeDO::getCode, code)));
    }

    /**
     * {@code findAll} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<KnowledgeBO> findAll(TenantId tenantId) {
        return convertList(
                knowledgeMapper.selectListByQuery(base(tenantId).eq(KnowledgeDO::getStatus, 1)));
    }

    /**
     * {@code searchByName} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param keyword 参数值，用于执行当前操作。
     * @param topK 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code page} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param offset 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<KnowledgeBO> page(TenantId tenantId, int offset, int limit) {
        return page(tenantId, offset, limit, null, null);
    }

    /**
     * {@code page} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param offset 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     * @param category 参数值，用于执行当前操作。
     * @param keyword 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code count} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public long count(TenantId tenantId) {
        return count(tenantId, null, null);
    }

    /**
     * {@code count} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param category 参数值，用于执行当前操作。
     * @param keyword 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public long count(TenantId tenantId, String category, String keyword) {
        QueryWrapper query = base(tenantId).eq(KnowledgeDO::getStatus, 1);
        if (category != null && !category.isBlank()) query.eq(KnowledgeDO::getCategory, category);
        if (keyword != null && !keyword.isBlank()) query.like(KnowledgeDO::getName, keyword);
        return knowledgeMapper.selectCountByQuery(query);
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
    public int insert(TenantId tenantId, KnowledgeBO bo) {
        bo.setTenantId(tenantId.value());
        KnowledgeDO data = MapstructUtils.convert(bo, KnowledgeDO.class);
        data.setTenantId(tenantId.value());
        int rows = knowledgeMapper.insert(data);
        bo.setId(data.getId());
        return rows;
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param bo 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code deleteById} 释放或移除当前操作涉及的资源。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public int deleteById(TenantId tenantId, Long id) {
        return knowledgeMapper.deleteByQuery(base(tenantId).eq(KnowledgeDO::getId, id));
    }

    /**
     * {@code existsByCode} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean existsByCode(TenantId tenantId, String code) {
        return findByCode(tenantId, code) != null;
    }

    /**
     * {@code existsBySpaceAndCode} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code findBySpace} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code pageBySpace} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     * @param keyword 参数值，用于执行当前操作。
     * @param category 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code deleteByIdAndSpace} 释放或移除当前操作涉及的资源。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public int deleteByIdAndSpace(TenantId tenantId, Long id, Long spaceId) {
        return knowledgeMapper.deleteByQuery(
                base(tenantId).eq(KnowledgeDO::getId, id).eq(KnowledgeDO::getSpaceId, spaceId));
    }

    /**
     * {@code assignDefaultSpace} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
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
        return QueryWrapper.create()
                .eq(KnowledgeDO::getTenantId, tenantId.value())
                .eq(KnowledgeDO::getDelFlag, 0);
    }

    private void requireTenant(TenantId tenantId, Long resourceTenantId) {
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
