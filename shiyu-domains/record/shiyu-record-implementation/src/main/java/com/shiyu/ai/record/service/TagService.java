package com.shiyu.ai.record.service;

import com.shiyu.ai.record.request.TagRequest;
import com.shiyu.ai.record.vo.TagVO;
import com.shiyu.ai.kernel.context.ActorContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface TagService {
    Pair<Long, List<TagVO>> pageView(ActorContext actor, Number pageNo, Number pageSize, String name);
    List<TagVO> allView(ActorContext actor);
    TagVO detailView(ActorContext actor, Long id);
    TagVO create(ActorContext actor, TagRequest request);
    boolean update(ActorContext actor, Long id, TagRequest request);
    boolean delete(ActorContext actor, Long id);
}
