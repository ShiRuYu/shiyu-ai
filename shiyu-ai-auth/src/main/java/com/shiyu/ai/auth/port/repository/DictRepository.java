package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.DictBO;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface DictRepository {
    Pair<Long, List<DictBO>> selectPage(Number pageNo, Number pageSize);
    List<DictBO> selectAll();
    DictBO selectById(Long id);
    List<DictBO> selectByDictType(String dictType);
    DictBO create(DictBO dictBO);
    DictBO update(DictBO dictBO);
    void deleteById(Long id);
    void deleteByIds(List<Long> ids);
}
