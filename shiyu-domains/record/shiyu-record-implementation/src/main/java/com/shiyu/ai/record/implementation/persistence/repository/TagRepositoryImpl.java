package com.shiyu.ai.record.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.record.implementation.persistence.dataobject.TagDO;
import com.shiyu.ai.record.implementation.persistence.mapper.TagMapper;
import com.shiyu.ai.record.domain.model.TagBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TagRepositoryImpl implements com.shiyu.ai.record.port.repository.TagRepository {

    @Resource
    private TagMapper tagMapper;

    public Pair<Long, List<TagBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, String name) {
        QueryWrapper countWrapper = new QueryWrapper();
        countWrapper.eq(TagDO::getTenantId, tenantId.value());
        if (name != null && !name.isBlank()) {
            countWrapper.like(TagDO::getName, name);
        }
        long total = tagMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(TagDO::getTenantId, tenantId.value());
        if (name != null && !name.isBlank()) {
            queryWrapper.like(TagDO::getName, name);
        }
        queryWrapper.orderBy(TagDO::getId, false);
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }

        List<TagDO> doList = tagMapper.selectListByQuery(queryWrapper);
        List<TagBO> boList = MapstructUtils.convert(doList, TagBO.class);
        return Pair.of(total, boList);
    }

    public TagBO selectById(TenantId tenantId, Long id) {
        TagDO d = tagMapper.selectOneByQuery(QueryWrapper.create()
                .eq(TagDO::getTenantId, tenantId.value())
                .eq(TagDO::getId, id));
        return MapstructUtils.convert(d, TagBO.class);
    }

    public TagBO selectByName(TenantId tenantId, String name) {
        QueryWrapper qw = new QueryWrapper().eq(TagDO::getTenantId, tenantId.value()).eq(TagDO::getName, name);
        TagDO d = tagMapper.selectOneByQuery(qw);
        return MapstructUtils.convert(d, TagBO.class);
    }

    public List<TagBO> selectAll(TenantId tenantId) {
        List<TagDO> list = tagMapper.selectListByQuery(new QueryWrapper()
                .eq(TagDO::getTenantId, tenantId.value()).orderBy(TagDO::getId, true));
        return MapstructUtils.convert(list, TagBO.class);
    }

    public TagBO insert(TenantId tenantId, TagBO tagBO) {
        TagDO d = MapstructUtils.convert(tagBO, TagDO.class);
        d.setTenantId(tenantId.value());
        tagMapper.insertSelective(d);
        tagBO.setId(d.getId());
        return tagBO;
    }

    public boolean update(TenantId tenantId, TagBO tagBO) {
        TagDO d = MapstructUtils.convert(tagBO, TagDO.class);
        d.setTenantId(tenantId.value());
        return tagMapper.updateByQuery(d, QueryWrapper.create()
                .eq(TagDO::getTenantId, tenantId.value()).eq(TagDO::getId, tagBO.getId())) > 0;
    }

    public boolean deleteById(TenantId tenantId, Long id) {
        return tagMapper.deleteByQuery(QueryWrapper.create()
                .eq(TagDO::getTenantId, tenantId.value()).eq(TagDO::getId, id)) > 0;
    }
}
