package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.dal.repository.record.MediaRepository;
import com.shiyu.ai.record.service.MediaService;
import com.shiyu.ai.record.bo.MediaBO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaServiceImpl implements MediaService {

    @Resource
    private MediaRepository mediaRepository;

    @Override
    public Pair<Long, List<MediaBO>> getPage(Number pageNo, Number pageSize, Long recordId) {
        if (pageNo == null || pageNo.intValue() < 1) pageNo = 1;
        if (pageSize == null || pageSize.intValue() < 1) pageSize = 10;
        return mediaRepository.selectPage(pageNo, pageSize, recordId);
    }

    @Override
    public MediaBO getById(Long id) {
        return mediaRepository.selectById(id);
    }

    @Override
    public MediaBO create(MediaBO mediaBO) {
        return mediaRepository.insert(mediaBO);
    }

    @Override
    public boolean update(MediaBO mediaBO) {
        return mediaRepository.update(mediaBO);
    }

    @Override
    public boolean delete(Long id) {
        return mediaRepository.deleteById(id);
    }
}
