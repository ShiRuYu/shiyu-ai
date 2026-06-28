package com.shiyu.ai.record.service;

import com.shiyu.ai.model.bo.TimelineEventBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 鏃堕棿杞翠簨浠舵湇鍔℃帴鍙?
 */
public interface TimelineEventService {

    /**
     * 鍒嗛〉鏌ヨ鏃堕棿杞翠簨浠跺垪琛?
     */
    Pair<Long, List<TimelineEventBO>> getPage(Number pageNo, Number pageSize, Long profileId);

    /**
     * 鏍规嵁ID鏌ヨ鏃堕棿杞翠簨浠?
     */
    TimelineEventBO getById(Long id);

    /**
     * 鍒涘缓鏃堕棿杞翠簨浠?
     */
    TimelineEventBO create(TimelineEventBO eventBO);

    /**
     * 鏇存柊鏃堕棿杞翠簨浠?
     */
    boolean update(TimelineEventBO eventBO);

    /**
     * 鍒犻櫎鏃堕棿杞翠簨浠?
     */
    boolean delete(Long id);

    /**
     * 鏌ヨ浜虹墿鐨勬椂闂磋酱
     */
    List<TimelineEventBO> getTimelineByProfileId(Long profileId);
}
