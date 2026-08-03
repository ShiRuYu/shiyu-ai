package com.shiyu.ai.record.service;

import com.shiyu.ai.record.request.RecordRequest;
import com.shiyu.ai.record.vo.RecordVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface RecordService {
    Pair<Long, List<RecordVO>> pageView(Number pageNo, Number pageSize, Long eventId);
    RecordVO detailView(Long id);
    RecordVO create(RecordRequest request);
    boolean update(Long id, RecordRequest request);
    boolean delete(Long id);
}
