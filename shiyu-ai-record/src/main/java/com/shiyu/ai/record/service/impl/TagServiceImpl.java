package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.dal.repository.record.TagRepository;
import com.shiyu.ai.record.service.TagService;
import com.shiyu.ai.dal.bo.record.TagBO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    @Resource
    private TagRepository tagRepository;

    @Override
    public Pair<Long, List<TagBO>> getPage(Number pageNo, Number pageSize, String name) {
        if (pageNo == null || pageNo.intValue() < 1) pageNo = 1;
        if (pageSize == null || pageSize.intValue() < 1) pageSize = 10;
        return tagRepository.selectPage(pageNo, pageSize, name);
    }

    @Override
    public TagBO getById(Long id) {
        return tagRepository.selectById(id);
    }

    @Override
    public TagBO getByName(String name) {
        return tagRepository.selectByName(name);
    }

    @Override
    public List<TagBO> getAll() {
        return tagRepository.selectAll();
    }

    @Override
    public TagBO create(TagBO tagBO) {
        return tagRepository.insert(tagBO);
    }

    @Override
    public boolean update(TagBO tagBO) {
        return tagRepository.update(tagBO);
    }

    @Override
    public boolean delete(Long id) {
        return tagRepository.deleteById(id);
    }
}
