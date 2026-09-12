package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocRelationDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocumentDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDocRelationMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDocumentMapper;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@code KnowledgeDocumentRepositoryImpl} 实现知识模块的持久化端口，负责在领域对象与存储模型之间转换。
 */
@Component
public class KnowledgeDocumentRepositoryImpl implements KnowledgeDocumentRepository {
    /**
     * knowledgeDocumentMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private KnowledgeDocumentMapper knowledgeDocumentMapper;

    /**
     * knowledgeDocRelationMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private KnowledgeDocRelationMapper knowledgeDocRelationMapper;

    @Override
    public KnowledgeDocumentBO selectById(TenantId tenantId, Long id) {
        return convert(
                knowledgeDocumentMapper.selectOneByQuery(
                        base(tenantId).eq(KnowledgeDocumentDO::getId, id)));
    }

    /**
     * {@code selectAll} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<KnowledgeDocumentBO> selectAll(TenantId tenantId) {
        return convertList(knowledgeDocumentMapper.selectListByQuery(base(tenantId)));
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
    public int insert(TenantId tenantId, KnowledgeDocumentBO bo) {
        bo.setTenantId(tenantId.value());
        KnowledgeDocumentDO data = MapstructUtils.convert(bo, KnowledgeDocumentDO.class);
        data.setTenantId(tenantId.value());
        int rows = knowledgeDocumentMapper.insert(data);
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
    public int update(TenantId tenantId, KnowledgeDocumentBO bo) {
        requireTenant(tenantId, bo.getTenantId());
        QueryWrapper scope = base(tenantId).eq(KnowledgeDocumentDO::getId, bo.getId());
        if (knowledgeDocumentMapper.selectOneByQuery(scope) == null) return 0;
        KnowledgeDocumentDO data = MapstructUtils.convert(bo, KnowledgeDocumentDO.class);
        data.setTenantId(tenantId.value());
        return knowledgeDocumentMapper.updateByQuery(data, scope);
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
        return knowledgeDocumentMapper.deleteByQuery(
                base(tenantId).eq(KnowledgeDocumentDO::getId, id));
    }

    /**
     * {@code searchByKeyword} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param keyword 参数值，用于执行当前操作。
     * @param topK 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<KnowledgeDocumentBO> searchByKeyword(TenantId tenantId, String keyword, int topK) {
        return selectAll(tenantId).stream()
                .filter(
                        d ->
                                (d.getTitle() != null && d.getTitle().contains(keyword))
                                        || (d.getContent() != null
                                                && d.getContent().contains(keyword)))
                .limit(topK)
                .toList();
    }

    /**
     * {@code selectByKnowledgeId} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<KnowledgeDocumentBO> selectByKnowledgeId(TenantId tenantId, Long knowledgeId) {
        List<Long> docIds =
                knowledgeDocRelationMapper
                        .selectListByQuery(
                                relationBase(tenantId)
                                        .eq(KnowledgeDocRelationDO::getKnowledgeId, knowledgeId))
                        .stream()
                        .map(KnowledgeDocRelationDO::getDocId)
                        .toList();
        if (docIds.isEmpty()) return List.of();
        return convertList(
                knowledgeDocumentMapper.selectListByQuery(
                        base(tenantId).in(KnowledgeDocumentDO::getId, docIds)));
    }

    /**
     * {@code selectByKnowledgeId} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param knowledgeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<KnowledgeDocumentBO> selectByKnowledgeId(
            TenantId tenantId, Long spaceId, Long knowledgeId) {
        List<Long> docIds =
                knowledgeDocRelationMapper
                        .selectListByQuery(
                                relationBase(tenantId)
                                        .eq(KnowledgeDocRelationDO::getSpaceId, spaceId)
                                        .eq(KnowledgeDocRelationDO::getKnowledgeId, knowledgeId))
                        .stream()
                        .map(KnowledgeDocRelationDO::getDocId)
                        .toList();
        if (docIds.isEmpty()) return List.of();
        return convertList(
                knowledgeDocumentMapper.selectListByQuery(
                        base(tenantId)
                                .eq(KnowledgeDocumentDO::getSpaceId, spaceId)
                                .in(KnowledgeDocumentDO::getId, docIds)));
    }

    /**
     * {@code pageBySpace} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     * @param keyword 参数值，用于执行当前操作。
     * @param lifecycleStatus 参数值，用于执行当前操作。
     * @param parseStatus 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public PageData<KnowledgeDocumentBO> pageBySpace(
            TenantId tenantId,
            Long spaceId,
            int pageNum,
            int pageSize,
            String keyword,
            String lifecycleStatus,
            String parseStatus) {
        QueryWrapper query = base(tenantId).eq(KnowledgeDocumentDO::getSpaceId, spaceId);
        if (keyword != null && !keyword.isBlank())
            query.like(KnowledgeDocumentDO::getTitle, keyword);
        if (lifecycleStatus != null && !lifecycleStatus.isBlank())
            query.eq(KnowledgeDocumentDO::getLifecycleStatus, lifecycleStatus);
        if (parseStatus != null && !parseStatus.isBlank())
            query.eq(KnowledgeDocumentDO::getParseStatus, parseStatus);
        var page =
                knowledgeDocumentMapper.paginate(
                        pageNum, pageSize, query.orderBy(KnowledgeDocumentDO::getId, false));
        return new PageData<>(convertList(page.getRecords()), page.getTotalRow());
    }

    /**
     * {@code findBySpaceAndChecksum} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param checksum 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public KnowledgeDocumentBO findBySpaceAndChecksum(
            TenantId tenantId, Long spaceId, String checksum) {
        return convert(
                knowledgeDocumentMapper.selectOneByQuery(
                        base(tenantId)
                                .eq(KnowledgeDocumentDO::getSpaceId, spaceId)
                                .eq(KnowledgeDocumentDO::getChecksum, checksum)
                                .limit(1)));
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
    public List<KnowledgeDocumentBO> findBySpace(TenantId tenantId, Long spaceId) {
        return convertList(
                knowledgeDocumentMapper.selectListByQuery(
                        base(tenantId)
                                .eq(KnowledgeDocumentDO::getSpaceId, spaceId)
                                .orderBy(KnowledgeDocumentDO::getId, true)));
    }

    /**
     * {@code assignDefaultSpace} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     */
    @Override
    public void assignDefaultSpace(TenantId tenantId, Long spaceId) {
        List<KnowledgeDocumentDO> records =
                knowledgeDocumentMapper.selectListByQuery(
                        base(tenantId).isNull(KnowledgeDocumentDO::getSpaceId));
        for (KnowledgeDocumentDO record : records) {
            record.setSpaceId(spaceId);
            if (record.getLifecycleStatus() == null) record.setLifecycleStatus("PUBLISHED");
            if (record.getParseStatus() == null) record.setParseStatus("READY");
            knowledgeDocumentMapper.update(record);
        }
    }

    private QueryWrapper base(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        return QueryWrapper.create()
                .eq(KnowledgeDocumentDO::getTenantId, tenantId.value())
                .eq(KnowledgeDocumentDO::getDelFlag, 0);
    }

    private QueryWrapper relationBase(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        return QueryWrapper.create()
                .eq(KnowledgeDocRelationDO::getTenantId, tenantId.value())
                .eq(KnowledgeDocRelationDO::getDelFlag, 0);
    }

    private void requireTenant(TenantId tenantId, Long resourceTenantId) {
        if (resourceTenantId == null || tenantId.value() != resourceTenantId) {
            throw new IllegalArgumentException("document tenant does not match actor tenant");
        }
    }

    private KnowledgeDocumentBO convert(KnowledgeDocumentDO data) {
        return data == null ? null : MapstructUtils.convert(data, KnowledgeDocumentBO.class);
    }

    private List<KnowledgeDocumentBO> convertList(List<KnowledgeDocumentDO> data) {
        return MapstructUtils.convert(data, KnowledgeDocumentBO.class);
    }
}
