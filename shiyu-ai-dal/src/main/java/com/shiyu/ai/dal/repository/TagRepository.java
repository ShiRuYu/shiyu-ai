package com.shiyu.ai.dal.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.record.TagDO;
import com.shiyu.ai.dal.mapper.record.TagMapper;
import com.shiyu.ai.model.bo.TagBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TagRepository {

    @Resource
    private TagMapper tagMapper;

    public Pair<Long, List<TagBO>> selectPage(Number pageNo, Number pageSize, String name) {
        QueryWrapper countWrapper = new QueryWrapper();
        if (name != null && !name.isBlank()) {
            countWrapper.like(TagDO::getName, name);
        }
        long total = tagMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
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

    public TagBO selectById(Long id) {
        TagDO d = tagMapper.selectOneById(id);
        return MapstructUtils.convert(d, TagBO.class);
    }

    public TagBO selectByName(String name) {
        QueryWrapper qw = new QueryWrapper().eq(TagDO::getName, name);
        TagDO d = tagMapper.selectOneByQuery(qw);
        return MapstructUtils.convert(d, TagBO.class);
    }

    public List<TagBO> selectAll() {
        List<TagDO> list = tagMapper.selectListByQuery(new QueryWrapper().orderBy(TagDO::getId, true));
        return MapstructUtils.convert(list, TagBO.class);
    }

    public TagBO insert(TagBO tagBO) {
        TagDO d = MapstructUtils.convert(tagBO, TagDO.class);
        tagMapper.insertSelective(d);
        tagBO.setId(d.getId());
        return tagBO;
    }

    public boolean update(TagBO tagBO) {
        TagDO d = MapstructUtils.convert(tagBO, TagDO.class);
        return tagMapper.update(d) > 0;
    }

    public boolean deleteById(Long id) {
        return tagMapper.deleteById(id) > 0;
    }
}
