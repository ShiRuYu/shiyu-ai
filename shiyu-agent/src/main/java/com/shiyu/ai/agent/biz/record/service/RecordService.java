package com.shiyu.ai.agent.biz.record.service;

import com.shiyu.ai.model.bo.RecordBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface RecordService {
    Pair<Long, List<RecordBO>> getPage(Number pageNo, Number pageSize, Long eventId);
    RecordBO getById(Long id);
    RecordBO create(RecordBO recordBO);
    boolean update(RecordBO recordBO);
    boolean delete(Long id);
}
