package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.request.DictRequest;
import com.shiyu.ai.auth.vo.DictVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/** Dictionary application contract. */
public interface DictService {
    Pair<Long, List<DictVO>> pageView(Number pageNo, Number pageSize);
    List<DictVO> byTypeView(String dictType);
    DictVO create(DictRequest request);
    DictVO update(Long id, DictRequest request);
    void deleteById(Long id);
    void deleteByIds(List<Long> ids);
}
