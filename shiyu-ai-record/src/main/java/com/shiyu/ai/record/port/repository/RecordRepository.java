package com.shiyu.ai.record.port.repository;

import com.shiyu.ai.record.domain.model.RecordBO;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface RecordRepository {
    Pair<Long, List<RecordBO>> selectPage(Number pageNo, Number pageSize, Long eventId);
    RecordBO selectById(Long id);
    RecordBO insert(RecordBO recordBO);
    boolean update(RecordBO recordBO);
    boolean deleteById(Long id);
}
