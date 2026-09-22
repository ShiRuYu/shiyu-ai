package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
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
 * 负责 知识 文档 的持久化查询、保存和删除，并维护数据访问边界。
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
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<KnowledgeDocumentBO> selectAll(TenantId tenantId) {
        return convertList(knowledgeDocumentMapper.selectListByQuery(base(tenantId)));
    }

    /**
     * 创建或保存 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 更新或设置 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 删除或移除 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 文档 相关操作生成的结果数据。
     */
    @Override
    public int deleteById(TenantId tenantId, Long id) {
        return knowledgeDocumentMapper.deleteByQuery(
                base(tenantId).eq(KnowledgeDocumentDO::getId, id));
    }

    /**
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param lifecycleStatus 用于完成本次业务处理的 lifecycleStatus 参数。
     * @param parseStatus 用于完成本次业务处理的 parseStatus 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param checksum 用于完成本次业务处理的 checksum 参数。
     * @return 返回 知识 文档 相关操作生成的结果数据。
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
     * 查询 知识 文档 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 更新或设置 知识 文档 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
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
        TenantScope.requireMatches(tenantId);
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
