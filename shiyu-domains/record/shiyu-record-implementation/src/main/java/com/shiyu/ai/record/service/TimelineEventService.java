package com.shiyu.ai.record.service;

import com.shiyu.ai.record.request.TimelineEventRequest;
import com.shiyu.ai.record.vo.TimelineEventVO;
import com.shiyu.ai.kernel.context.ActorContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface TimelineEventService {
    Pair<Long, List<TimelineEventVO>> pageView(ActorContext actor, Number pageNo, Number pageSize, Long profileId);
    TimelineEventVO detailView(ActorContext actor, Long id);
    TimelineEventVO create(ActorContext actor, TimelineEventRequest request);
    boolean update(ActorContext actor, Long id, TimelineEventRequest request);
    boolean delete(ActorContext actor, Long id);
    List<TimelineEventVO> timelineView(ActorContext actor, Long profileId);
}
