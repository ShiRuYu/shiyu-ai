package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeMapper;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeRepositoryImpl implements KnowledgeRepository {
    @Resource
    private KnowledgeMapper knowledgeMapper;

    @Override
    public KnowledgeBO findById(TenantId tenantId, Long id) {
        return convert(knowledgeMapper.selectOneByQuery(base(tenantId).eq(KnowledgeDO::getId, id)));
    }

    @Override
    public KnowledgeBO findByCode(TenantId tenantId, String code) {
        return convert(knowledgeMapper.selectOneByQuery(base(tenantId).eq(KnowledgeDO::getCode, code)));
    }

    @Override
    public List<KnowledgeBO> findAll(TenantId tenantId) {
        return convertList(knowledgeMapper.selectListByQuery(base(tenantId).eq(KnowledgeDO::getStatus, 1)));
    }

    @Override
    public List<KnowledgeBO> searchByName(TenantId tenantId, String keyword, int topK) {
        return convertList(knowledgeMapper.selectListByQuery(base(tenantId)
                .eq(KnowledgeDO::getStatus, 1).like(KnowledgeDO::getName, keyword)
                .orderBy(KnowledgeDO::getId, true).limit(0, topK)));
    }

    @Override
    public List<KnowledgeBO> page(TenantId tenantId, int offset, int limit) {
        return page(tenantId, offset, limit, null, null);
    }

    @Override
    public List<KnowledgeBO> page(TenantId tenantId, int offset, int limit, String category, String keyword) {
        QueryWrapper query = base(tenantId).eq(KnowledgeDO::getStatus, 1);
        if (category != null && !category.isBlank()) query.eq(KnowledgeDO::getCategory, category);
        if (keyword != null && !keyword.isBlank()) query.like(KnowledgeDO::getName, keyword);
        return convertList(knowledgeMapper.selectListByQuery(
                query.orderBy(KnowledgeDO::getId, true).limit(offset, limit)));
    }

    @Override
    public long count(TenantId tenantId) {
        return count(tenantId, null, null);
    }

    @Override
    public long count(TenantId tenantId, String category, String keyword) {
        QueryWrapper query = base(tenantId).eq(KnowledgeDO::getStatus, 1);
        if (category != null && !category.isBlank()) query.eq(KnowledgeDO::getCategory, category);
        if (keyword != null && !keyword.isBlank()) query.like(KnowledgeDO::getName, keyword);
        return knowledgeMapper.selectCountByQuery(query);
    }

    @Override
    public int insert(TenantId tenantId, KnowledgeBO bo) {
        bo.setTenantId(tenantId.value());
        KnowledgeDO data = MapstructUtils.convert(bo, KnowledgeDO.class);
        data.setTenantId(tenantId.value());
        int rows = knowledgeMapper.insert(data);
        bo.setId(data.getId());
        return rows;
    }

    @Override
    public int update(TenantId tenantId, KnowledgeBO bo) {
        requireTenant(tenantId, bo.getTenantId());
        QueryWrapper scope = base(tenantId).eq(KnowledgeDO::getId, bo.getId());
        if (knowledgeMapper.selectOneByQuery(scope) == null) return 0;
        KnowledgeDO data = MapstructUtils.convert(bo, KnowledgeDO.class);
        data.setTenantId(tenantId.value());
        return knowledgeMapper.updateByQuery(data, scope);
    }

    @Override
    public int deleteById(TenantId tenantId, Long id) {
        return knowledgeMapper.deleteByQuery(base(tenantId).eq(KnowledgeDO::getId, id));
    }

    @Override
    public boolean existsByCode(TenantId tenantId, String code) {
        return findByCode(tenantId, code) != null;
    }

    @Override
    public boolean existsBySpaceAndCode(TenantId tenantId, Long spaceId, String code) {
        return knowledgeMapper.selectCountByQuery(base(tenantId)
                .eq(KnowledgeDO::getSpaceId, spaceId).eq(KnowledgeDO::getCode, code)) > 0;
    }

    @Override
    public List<KnowledgeBO> findBySpace(TenantId tenantId, Long spaceId) {
        return convertList(knowledgeMapper.selectListByQuery(base(tenantId)
                .eq(KnowledgeDO::getSpaceId, spaceId).eq(KnowledgeDO::getStatus, 1)));
    }

    @Override
    public PageData<KnowledgeBO> pageBySpace(TenantId tenantId, Long spaceId, int pageNum, int pageSize,
                                              String keyword, String category) {
        QueryWrapper query = base(tenantId).eq(KnowledgeDO::getSpaceId, spaceId)
                .eq(KnowledgeDO::getStatus, 1);
        if (keyword != null && !keyword.isBlank()) query.like(KnowledgeDO::getName, keyword);
        if (category != null && !category.isBlank()) query.eq(KnowledgeDO::getCategory, category);
        var page = knowledgeMapper.paginate(pageNum, pageSize, query.orderBy(KnowledgeDO::getId, false));
        return new PageData<>(convertList(page.getRecords()), page.getTotalRow());
    }

    @Override
    public int deleteByIdAndSpace(TenantId tenantId, Long id, Long spaceId) {
        return knowledgeMapper.deleteByQuery(base(tenantId)
                .eq(KnowledgeDO::getId, id).eq(KnowledgeDO::getSpaceId, spaceId));
    }

    @Override
    public void assignDefaultSpace(TenantId tenantId, Long spaceId) {
        List<KnowledgeDO> records = knowledgeMapper.selectListByQuery(
                base(tenantId).isNull(KnowledgeDO::getSpaceId));
        for (KnowledgeDO record : records) {
            record.setSpaceId(spaceId);
            knowledgeMapper.update(record);
        }
    }

    private QueryWrapper base(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        return QueryWrapper.create().eq(KnowledgeDO::getTenantId, tenantId.value())
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
