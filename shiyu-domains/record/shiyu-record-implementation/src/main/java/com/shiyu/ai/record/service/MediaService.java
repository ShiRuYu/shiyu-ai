package com.shiyu.ai.record.service;

import com.shiyu.ai.record.request.MediaRequest;
import com.shiyu.ai.record.vo.MediaVO;
import com.shiyu.ai.kernel.context.ActorContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface MediaService {
    Pair<Long, List<MediaVO>> pageView(ActorContext actor, Number pageNo, Number pageSize, Long recordId);
    MediaVO detailView(ActorContext actor, Long id);
    MediaVO create(ActorContext actor, MediaRequest request);
    boolean update(ActorContext actor, Long id, MediaRequest request);
    boolean delete(ActorContext actor, Long id);
}
