package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.record.domain.model.TimelineEventBO;
import com.shiyu.ai.record.port.repository.TimelineEventRepository;
import com.shiyu.ai.record.request.TimelineEventRequest;
import com.shiyu.ai.record.service.TimelineEventService;
import com.shiyu.ai.record.vo.TimelineEventVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public class TimelineEventServiceImpl implements TimelineEventService {
    private final TimelineEventRepository timelineEventRepository;

    public TimelineEventServiceImpl(TimelineEventRepository timelineEventRepository) {
        this.timelineEventRepository = timelineEventRepository;
    }
    @Override public Pair<Long,List<TimelineEventVO>> pageView(ActorContext actor,Number n,Number s,Long p){actor=requireActor(actor);if(n==null||n.intValue()<1)n=1;if(s==null||s.intValue()<1)s=20;var x=timelineEventRepository.selectPage(actor.tenantId(),n,s,p);return Pair.of(x.getLeft(),MapstructUtils.convert(x.getRight(),TimelineEventVO.class));}
    @Override public TimelineEventVO detailView(ActorContext actor,Long id){return MapstructUtils.convert(timelineEventRepository.selectByIdWithDetails(requireActor(actor).tenantId(),id),TimelineEventVO.class);}
    @Override public TimelineEventVO create(ActorContext actor,TimelineEventRequest r){TimelineEventBO b=toBO(r);return MapstructUtils.convert(timelineEventRepository.insert(requireActor(actor).tenantId(),b),TimelineEventVO.class);}
    @Override public boolean update(ActorContext actor,Long id,TimelineEventRequest r){actor=requireActor(actor);TimelineEventBO b=timelineEventRepository.selectByIdWithDetails(actor.tenantId(),id);if(b==null)return false;TimelineEventBO incoming=toBO(r);b.setTitle(incoming.getTitle());b.setType(incoming.getType());if(incoming.getEventTime()!=null)b.setEventTime(incoming.getEventTime());return timelineEventRepository.update(actor.tenantId(),b);}
    @Override public boolean delete(ActorContext actor,Long id){return timelineEventRepository.deleteById(requireActor(actor).tenantId(),id);}
    @Override public List<TimelineEventVO> timelineView(ActorContext actor,Long profileId){return MapstructUtils.convert(timelineEventRepository.selectByProfileId(requireActor(actor).tenantId(),profileId),TimelineEventVO.class);}
    private static TimelineEventBO toBO(TimelineEventRequest r){TimelineEventBO b=new TimelineEventBO();b.setProfileId(r.getProfileId());b.setTitle(r.getTitle());b.setType(r.getEventType());if(r.getEventTime()!=null)b.setEventTime(r.getEventTime());else if(r.getEventDate()!=null)b.setEventTime(r.getEventDate());return b;}
    private static ActorContext requireActor(ActorContext actor){return Objects.requireNonNull(actor,"actor is required");}
}
