package com.shiyu.ai.record.service;

import com.shiyu.ai.record.request.RecordRequest;
import com.shiyu.ai.record.vo.RecordVO;
import com.shiyu.ai.kernel.context.ActorContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface RecordService {
    Pair<Long, List<RecordVO>> pageView(ActorContext actor, Number pageNo, Number pageSize, Long eventId);
    RecordVO detailView(ActorContext actor, Long id);
    RecordVO create(ActorContext actor, RecordRequest request);
    boolean update(ActorContext actor, Long id, RecordRequest request);
    boolean delete(ActorContext actor, Long id);
}
