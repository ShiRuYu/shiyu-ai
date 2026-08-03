package com.shiyu.ai.record.service;

import com.shiyu.ai.record.request.TimelineEventRequest;
import com.shiyu.ai.record.vo.TimelineEventVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface TimelineEventService {
    Pair<Long, List<TimelineEventVO>> pageView(Number pageNo, Number pageSize, Long profileId);
    TimelineEventVO detailView(Long id);
    TimelineEventVO create(TimelineEventRequest request);
    boolean update(Long id, TimelineEventRequest request);
    boolean delete(Long id);
    List<TimelineEventVO> timelineView(Long profileId);
}
