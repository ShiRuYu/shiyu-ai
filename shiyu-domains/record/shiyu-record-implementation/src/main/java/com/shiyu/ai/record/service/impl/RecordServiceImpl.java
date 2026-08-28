package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.record.domain.model.RecordBO;
import com.shiyu.ai.record.port.repository.RecordRepository;
import com.shiyu.ai.record.request.RecordRequest;
import com.shiyu.ai.record.service.RecordService;
import com.shiyu.ai.record.vo.RecordVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
public class RecordServiceImpl implements RecordService {
    private final RecordRepository recordRepository;
    public RecordServiceImpl(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }
    @Override public Pair<Long,List<RecordVO>> pageView(ActorContext actor,Number n,Number s,Long e){actor=requireActor(actor);if(n==null||n.intValue()<1)n=1;if(s==null||s.intValue()<1)s=10;var p=recordRepository.selectPage(actor.tenantId(),n,s,e);return Pair.of(p.getLeft(),MapstructUtils.convert(p.getRight(),RecordVO.class));}
    @Override public RecordVO detailView(ActorContext actor,Long id){return MapstructUtils.convert(recordRepository.selectById(requireActor(actor).tenantId(),id),RecordVO.class);}
    @Override @Transactional(rollbackFor=Exception.class) public RecordVO create(ActorContext actor,RecordRequest r){actor=requireActor(actor);RecordBO b=new RecordBO();b.setEventId(r.getEventId());b.setContent(r.getContent());return MapstructUtils.convert(recordRepository.insert(actor.tenantId(),b),RecordVO.class);}
    @Override @Transactional(rollbackFor=Exception.class) public boolean update(ActorContext actor,Long id,RecordRequest r){actor=requireActor(actor);RecordBO b=recordRepository.selectById(actor.tenantId(),id);if(b==null)return false;b.setEventId(r.getEventId());b.setContent(r.getContent());return recordRepository.update(actor.tenantId(),b);}
    @Override @Transactional(rollbackFor=Exception.class) public boolean delete(ActorContext actor,Long id){return recordRepository.deleteById(requireActor(actor).tenantId(),id);}
    private static ActorContext requireActor(ActorContext actor){return Objects.requireNonNull(actor,"actor is required");}
}
