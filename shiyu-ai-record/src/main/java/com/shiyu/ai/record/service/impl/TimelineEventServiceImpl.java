package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.record.domain.model.TimelineEventBO;
import com.shiyu.ai.record.port.repository.TimelineEventRepository;
import com.shiyu.ai.record.service.TimelineEventService;
import com.shiyu.ai.record.request.TimelineEventRequest;
import com.shiyu.ai.record.vo.TimelineEventVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 鏃堕棿杞翠簨浠舵湇鍔″疄鐜?
 */
@Service
public class TimelineEventServiceImpl implements TimelineEventService {
    @Override public Pair<Long, List<TimelineEventVO>> pageView(Number n, Number s, Long p) { var x=getPage(n,s,p); return Pair.of(x.getLeft(), MapstructUtils.convert(x.getRight(), TimelineEventVO.class)); }
    @Override public TimelineEventVO detailView(Long id) { return MapstructUtils.convert(getById(id), TimelineEventVO.class); }
    @Override public TimelineEventVO create(TimelineEventRequest r) { TimelineEventBO b=new TimelineEventBO(); b.setProfileId(r.getProfileId()); b.setTitle(r.getTitle()); b.setType(r.getEventType()); if(r.getEventDate()!=null)b.setEventTime(r.getEventDate()); return MapstructUtils.convert(create(b), TimelineEventVO.class); }
    @Override public boolean update(Long id, TimelineEventRequest r) { TimelineEventBO b=getById(id); if(b==null)return false; b.setTitle(r.getTitle()); b.setType(r.getEventType()); if(r.getEventDate()!=null)b.setEventTime(r.getEventDate()); return update(b); }
    @Override public List<TimelineEventVO> timelineView(Long id) { return MapstructUtils.convert(getTimelineByProfileId(id), TimelineEventVO.class); }

    @Resource
    private TimelineEventRepository timelineEventRepository;

    private Pair<Long, List<TimelineEventBO>> getPage(Number pageNo, Number pageSize, Long profileId) {
        if (pageNo == null || pageNo.intValue() < 1) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize.intValue() < 1) {
            pageSize = 20;
        }
        return timelineEventRepository.selectPage(pageNo, pageSize, profileId);
    }

    private TimelineEventBO getById(Long id) {
        return timelineEventRepository.selectByIdWithDetails(id);
    }

    private TimelineEventBO create(TimelineEventBO eventBO) {
        return timelineEventRepository.insert(eventBO);
    }

    private boolean update(TimelineEventBO eventBO) {
        return timelineEventRepository.update(eventBO);
    }

    @Override
    public boolean delete(Long id) {
        return timelineEventRepository.deleteById(id);
    }

    private List<TimelineEventBO> getTimelineByProfileId(Long profileId) {
        return timelineEventRepository.selectByProfileId(profileId);
    }
}
