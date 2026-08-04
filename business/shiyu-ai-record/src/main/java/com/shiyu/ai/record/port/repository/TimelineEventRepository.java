package com.shiyu.ai.record.port.repository;

import com.shiyu.ai.record.domain.model.TimelineEventBO;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface TimelineEventRepository {
    Pair<Long, List<TimelineEventBO>> selectPage(Number pageNo, Number pageSize, Long profileId);
    TimelineEventBO selectByIdWithDetails(Long id);
    TimelineEventBO insert(TimelineEventBO eventBO);
    boolean update(TimelineEventBO eventBO);
    boolean deleteById(Long id);
    List<TimelineEventBO> selectByProfileId(Long profileId);
}
