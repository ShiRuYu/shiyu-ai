package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.record.bo.TimelineEventBO;
import com.shiyu.ai.record.repository.TimelineEventRepository;
import com.shiyu.ai.record.service.TimelineEventService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 鏃堕棿杞翠簨浠舵湇鍔″疄鐜?
 */
@Service
public class TimelineEventServiceImpl implements TimelineEventService {

    @Resource
    private TimelineEventRepository timelineEventRepository;

    @Override
    public Pair<Long, List<TimelineEventBO>> getPage(Number pageNo, Number pageSize, Long profileId) {
        if (pageNo == null || pageNo.intValue() < 1) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize.intValue() < 1) {
            pageSize = 20;
        }
        return timelineEventRepository.selectPage(pageNo, pageSize, profileId);
    }

    @Override
    public TimelineEventBO getById(Long id) {
        return timelineEventRepository.selectByIdWithDetails(id);
    }

    @Override
    public TimelineEventBO create(TimelineEventBO eventBO) {
        return timelineEventRepository.insert(eventBO);
    }

    @Override
    public boolean update(TimelineEventBO eventBO) {
        return timelineEventRepository.update(eventBO);
    }

    @Override
    public boolean delete(Long id) {
        return timelineEventRepository.deleteById(id);
    }

    @Override
    public List<TimelineEventBO> getTimelineByProfileId(Long profileId) {
        return timelineEventRepository.selectByProfileId(profileId);
    }
}
