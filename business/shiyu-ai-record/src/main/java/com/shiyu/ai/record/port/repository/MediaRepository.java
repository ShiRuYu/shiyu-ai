package com.shiyu.ai.record.port.repository;

import com.shiyu.ai.record.domain.model.MediaBO;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface MediaRepository {
    Pair<Long, List<MediaBO>> selectPage(Number pageNo, Number pageSize, Long recordId);
    MediaBO selectById(Long id);
    MediaBO insert(MediaBO mediaBO);
    boolean update(MediaBO mediaBO);
    boolean deleteById(Long id);
}
