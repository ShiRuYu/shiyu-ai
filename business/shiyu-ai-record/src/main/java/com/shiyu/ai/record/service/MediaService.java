package com.shiyu.ai.record.service;

import com.shiyu.ai.record.request.MediaRequest;
import com.shiyu.ai.record.vo.MediaVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface MediaService {
    Pair<Long, List<MediaVO>> pageView(Number pageNo, Number pageSize, Long recordId);
    MediaVO detailView(Long id);
    MediaVO create(MediaRequest request);
    boolean update(Long id, MediaRequest request);
    boolean delete(Long id);
}
