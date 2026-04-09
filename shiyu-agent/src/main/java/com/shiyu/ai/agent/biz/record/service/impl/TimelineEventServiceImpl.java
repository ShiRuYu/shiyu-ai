package com.shiyu.ai.agent.biz.record.service.impl;

import com.shiyu.ai.agent.domain.bo.TimelineEventBO;
import com.shiyu.ai.agent.biz.record.repository.TimelineEventRepository;
import com.shiyu.ai.agent.biz.record.service.TimelineEventService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 时间轴事件服务实现
 */
@Service
public class TimelineEventServiceImpl implements TimelineEventService {

    @Resource
    private TimelineEventRepository timelineEventRepository;

    @Override
    public Pair<Long, List<TimelineEventBO>> getPage(Integer pageNo, Integer pageSize, Long profileId) {
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize < 1) {
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
