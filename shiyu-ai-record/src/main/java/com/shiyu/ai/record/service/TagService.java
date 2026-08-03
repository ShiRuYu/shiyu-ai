package com.shiyu.ai.record.service;

import com.shiyu.ai.record.request.TagRequest;
import com.shiyu.ai.record.vo.TagVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface TagService {
    Pair<Long, List<TagVO>> pageView(Number pageNo, Number pageSize, String name);
    List<TagVO> allView();
    TagVO detailView(Long id);
    TagVO create(TagRequest request);
    boolean update(Long id, TagRequest request);
    boolean delete(Long id);
}
