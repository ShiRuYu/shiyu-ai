package com.shiyu.ai.record.port.repository;

import com.shiyu.ai.record.domain.model.TagBO;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface TagRepository {
    Pair<Long, List<TagBO>> selectPage(Number pageNo, Number pageSize, String name);
    TagBO selectById(Long id);
    TagBO selectByName(String name);
    List<TagBO> selectAll();
    TagBO insert(TagBO tagBO);
    boolean update(TagBO tagBO);
    boolean deleteById(Long id);
}
