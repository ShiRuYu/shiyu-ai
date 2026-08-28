package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.request.DictRequest;
import com.shiyu.ai.auth.vo.DictVO;
import com.shiyu.ai.kernel.context.ActorContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/** Dictionary application contract. */
public interface DictService {
    Pair<Long, List<DictVO>> pageView(ActorContext actor, Number pageNo, Number pageSize);
    List<DictVO> byTypeView(ActorContext actor, String dictType);
    DictVO create(ActorContext actor, DictRequest request);
    DictVO update(ActorContext actor, Long id, DictRequest request);
    void deleteById(ActorContext actor, Long id);
    void deleteByIds(ActorContext actor, List<Long> ids);
}
