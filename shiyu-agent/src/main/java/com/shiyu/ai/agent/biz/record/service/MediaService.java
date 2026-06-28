package com.shiyu.ai.agent.biz.record.service;

import com.shiyu.ai.model.bo.MediaBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface MediaService {
    Pair<Long, List<MediaBO>> getPage(Number pageNo, Number pageSize, Long recordId);
    MediaBO getById(Long id);
    MediaBO create(MediaBO mediaBO);
    boolean update(MediaBO mediaBO);
    boolean delete(Long id);
}
