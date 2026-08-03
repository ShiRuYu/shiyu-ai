package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.record.port.repository.TagRepository;
import com.shiyu.ai.record.service.TagService;
import com.shiyu.ai.record.request.TagRequest;
import com.shiyu.ai.record.vo.TagVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.record.domain.model.TagBO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {
    @Override public Pair<Long, List<TagVO>> pageView(Number n, Number s, String name) { var p=getPage(n,s,name); return Pair.of(p.getLeft(), MapstructUtils.convert(p.getRight(), TagVO.class)); }
    @Override public TagVO detailView(Long id) { return MapstructUtils.convert(getById(id), TagVO.class); }
    @Override public List<TagVO> allView() { return MapstructUtils.convert(getAll(), TagVO.class); }
    @Override public TagVO create(TagRequest r) { TagBO b=new TagBO(); b.setName(r.getName()); return MapstructUtils.convert(create(b), TagVO.class); }
    @Override public boolean update(Long id, TagRequest r) { TagBO b=getById(id); if(b==null)return false; b.setName(r.getName()); return update(b); }

    @Resource
    private TagRepository tagRepository;

    private Pair<Long, List<TagBO>> getPage(Number pageNo, Number pageSize, String name) {
        if (pageNo == null || pageNo.intValue() < 1) pageNo = 1;
        if (pageSize == null || pageSize.intValue() < 1) pageSize = 10;
        return tagRepository.selectPage(pageNo, pageSize, name);
    }

    private TagBO getById(Long id) {
        return tagRepository.selectById(id);
    }

    private TagBO getByName(String name) {
        return tagRepository.selectByName(name);
    }

    private List<TagBO> getAll() {
        return tagRepository.selectAll();
    }

    private TagBO create(TagBO tagBO) {
        return tagRepository.insert(tagBO);
    }

    private boolean update(TagBO tagBO) {
        return tagRepository.update(tagBO);
    }

    @Override
    public boolean delete(Long id) {
        return tagRepository.deleteById(id);
    }
}
