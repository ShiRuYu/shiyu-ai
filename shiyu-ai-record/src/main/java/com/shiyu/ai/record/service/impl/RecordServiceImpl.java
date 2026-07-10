package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.dal.repository.record.RecordRepository;
import com.shiyu.ai.record.service.RecordService;
import com.shiyu.ai.dal.bo.record.RecordBO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecordServiceImpl implements RecordService {

    @Resource
    private RecordRepository recordRepository;

    @Override
    public Pair<Long, List<RecordBO>> getPage(Number pageNo, Number pageSize, Long eventId) {
        if (pageNo == null || pageNo.intValue() < 1) pageNo = 1;
        if (pageSize == null || pageSize.intValue() < 1) pageSize = 10;
        return recordRepository.selectPage(pageNo, pageSize, eventId);
    }

    @Override
    public RecordBO getById(Long id) {
        return recordRepository.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RecordBO create(RecordBO recordBO) {
        return recordRepository.insert(recordBO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(RecordBO recordBO) {
        return recordRepository.update(recordBO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        return recordRepository.deleteById(id);
    }
}
