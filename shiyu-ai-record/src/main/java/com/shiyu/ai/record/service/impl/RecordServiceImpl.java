package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.record.port.repository.RecordRepository;
import com.shiyu.ai.record.service.RecordService;
import com.shiyu.ai.record.request.RecordRequest;
import com.shiyu.ai.record.vo.RecordVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.record.domain.model.RecordBO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecordServiceImpl implements RecordService {
    @Override public Pair<Long, List<RecordVO>> pageView(Number n, Number s, Long e) { var p=getPage(n,s,e); return Pair.of(p.getLeft(), MapstructUtils.convert(p.getRight(), RecordVO.class)); }
    @Override public RecordVO detailView(Long id) { return MapstructUtils.convert(getById(id), RecordVO.class); }
    @Override public RecordVO create(RecordRequest r) { RecordBO b=new RecordBO(); b.setEventId(r.getEventId()); b.setContent(r.getContent()); return MapstructUtils.convert(create(b), RecordVO.class); }
    @Override public boolean update(Long id, RecordRequest r) { RecordBO b=getById(id); if(b==null)return false; b.setEventId(r.getEventId()); b.setContent(r.getContent()); return update(b); }

    @Resource
    private RecordRepository recordRepository;

    private Pair<Long, List<RecordBO>> getPage(Number pageNo, Number pageSize, Long eventId) {
        if (pageNo == null || pageNo.intValue() < 1) pageNo = 1;
        if (pageSize == null || pageSize.intValue() < 1) pageSize = 10;
        return recordRepository.selectPage(pageNo, pageSize, eventId);
    }

    private RecordBO getById(Long id) {
        return recordRepository.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    private RecordBO create(RecordBO recordBO) {
        return recordRepository.insert(recordBO);
    }

    @Transactional(rollbackFor = Exception.class)
    private boolean update(RecordBO recordBO) {
        return recordRepository.update(recordBO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        return recordRepository.deleteById(id);
    }
}
