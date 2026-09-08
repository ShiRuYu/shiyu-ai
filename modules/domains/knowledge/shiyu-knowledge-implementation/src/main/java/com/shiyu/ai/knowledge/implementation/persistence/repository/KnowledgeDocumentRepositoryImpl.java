package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocRelationDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocumentDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDocRelationMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDocumentMapper;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeDocumentRepositoryImpl implements KnowledgeDocumentRepository {
    @Resource
    private KnowledgeDocumentMapper knowledgeDocumentMapper;
    @Resource
    private KnowledgeDocRelationMapper knowledgeDocRelationMapper;

    @Override
    public KnowledgeDocumentBO selectById(TenantId tenantId, Long id) {
        return convert(knowledgeDocumentMapper.selectOneByQuery(base(tenantId).eq(KnowledgeDocumentDO::getId, id)));
    }

    @Override
    public List<KnowledgeDocumentBO> selectAll(TenantId tenantId) {
        return convertList(knowledgeDocumentMapper.selectListByQuery(base(tenantId)));
    }

    @Override
    public int insert(TenantId tenantId, KnowledgeDocumentBO bo) {
        bo.setTenantId(tenantId.value());
        KnowledgeDocumentDO data = MapstructUtils.convert(bo, KnowledgeDocumentDO.class);
        data.setTenantId(tenantId.value());
        int rows = knowledgeDocumentMapper.insert(data);
        bo.setId(data.getId());
        return rows;
    }

    @Override
    public int update(TenantId tenantId, KnowledgeDocumentBO bo) {
        requireTenant(tenantId, bo.getTenantId());
        QueryWrapper scope = base(tenantId).eq(KnowledgeDocumentDO::getId, bo.getId());
        if (knowledgeDocumentMapper.selectOneByQuery(scope) == null) return 0;
        KnowledgeDocumentDO data = MapstructUtils.convert(bo, KnowledgeDocumentDO.class);
        data.setTenantId(tenantId.value());
        return knowledgeDocumentMapper.updateByQuery(data, scope);
    }

    @Override
    public int deleteById(TenantId tenantId, Long id) {
        return knowledgeDocumentMapper.deleteByQuery(base(tenantId).eq(KnowledgeDocumentDO::getId, id));
    }

    @Override
    public List<KnowledgeDocumentBO> searchByKeyword(TenantId tenantId, String keyword, int topK) {
        return selectAll(tenantId).stream()
                .filter(d -> (d.getTitle() != null && d.getTitle().contains(keyword))
                        || (d.getContent() != null && d.getContent().contains(keyword)))
                .limit(topK).toList();
    }

    @Override
    public List<KnowledgeDocumentBO> selectByKnowledgeId(TenantId tenantId, Long knowledgeId) {
        List<Long> docIds = knowledgeDocRelationMapper.selectListByQuery(relationBase(tenantId)
                        .eq(KnowledgeDocRelationDO::getKnowledgeId, knowledgeId))
                .stream().map(KnowledgeDocRelationDO::getDocId).toList();
        if (docIds.isEmpty()) return List.of();
        return convertList(knowledgeDocumentMapper.selectListByQuery(base(tenantId)
                .in(KnowledgeDocumentDO::getId, docIds)));
    }

    @Override
    public List<KnowledgeDocumentBO> selectByKnowledgeId(TenantId tenantId, Long spaceId, Long knowledgeId) {
        List<Long> docIds = knowledgeDocRelationMapper.selectListByQuery(relationBase(tenantId)
                        .eq(KnowledgeDocRelationDO::getSpaceId, spaceId)
                        .eq(KnowledgeDocRelationDO::getKnowledgeId, knowledgeId))
                .stream().map(KnowledgeDocRelationDO::getDocId).toList();
        if (docIds.isEmpty()) return List.of();
        return convertList(knowledgeDocumentMapper.selectListByQuery(base(tenantId)
                .eq(KnowledgeDocumentDO::getSpaceId, spaceId)
                .in(KnowledgeDocumentDO::getId, docIds)));
    }

    @Override
    public PageData<KnowledgeDocumentBO> pageBySpace(TenantId tenantId, Long spaceId, int pageNum, int pageSize,
                                                     String keyword, String lifecycleStatus, String parseStatus) {
        QueryWrapper query = base(tenantId).eq(KnowledgeDocumentDO::getSpaceId, spaceId);
        if (keyword != null && !keyword.isBlank()) query.like(KnowledgeDocumentDO::getTitle, keyword);
        if (lifecycleStatus != null && !lifecycleStatus.isBlank()) query.eq(KnowledgeDocumentDO::getLifecycleStatus, lifecycleStatus);
        if (parseStatus != null && !parseStatus.isBlank()) query.eq(KnowledgeDocumentDO::getParseStatus, parseStatus);
        var page = knowledgeDocumentMapper.paginate(pageNum, pageSize,
                query.orderBy(KnowledgeDocumentDO::getId, false));
        return new PageData<>(convertList(page.getRecords()), page.getTotalRow());
    }

    @Override
    public KnowledgeDocumentBO findBySpaceAndChecksum(TenantId tenantId, Long spaceId, String checksum) {
        return convert(knowledgeDocumentMapper.selectOneByQuery(base(tenantId)
                .eq(KnowledgeDocumentDO::getSpaceId, spaceId)
                .eq(KnowledgeDocumentDO::getChecksum, checksum).limit(1)));
    }

    @Override
    public List<KnowledgeDocumentBO> findBySpace(TenantId tenantId, Long spaceId) {
        return convertList(knowledgeDocumentMapper.selectListByQuery(base(tenantId)
                .eq(KnowledgeDocumentDO::getSpaceId, spaceId)
                .orderBy(KnowledgeDocumentDO::getId, true)));
    }

    @Override
    public void assignDefaultSpace(TenantId tenantId, Long spaceId) {
        List<KnowledgeDocumentDO> records = knowledgeDocumentMapper.selectListByQuery(
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
        return QueryWrapper.create().eq(KnowledgeDocumentDO::getTenantId, tenantId.value())
                .eq(KnowledgeDocumentDO::getDelFlag, 0);
    }

    private QueryWrapper relationBase(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        return QueryWrapper.create().eq(KnowledgeDocRelationDO::getTenantId, tenantId.value())
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
