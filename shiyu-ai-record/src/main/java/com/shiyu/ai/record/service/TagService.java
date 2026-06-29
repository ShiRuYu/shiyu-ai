package com.shiyu.ai.record.service;

import com.shiyu.ai.record.bo.TagBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface TagService {
    Pair<Long, List<TagBO>> getPage(Number pageNo, Number pageSize, String name);
    TagBO getById(Long id);
    TagBO getByName(String name);
    List<TagBO> getAll();
    TagBO create(TagBO tagBO);
    boolean update(TagBO tagBO);
    boolean delete(Long id);
}
