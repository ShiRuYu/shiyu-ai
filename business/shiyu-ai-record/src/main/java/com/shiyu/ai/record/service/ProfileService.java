package com.shiyu.ai.record.service;

import com.shiyu.ai.record.request.ProfileRequest;
import com.shiyu.ai.record.vo.ProfileVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface ProfileService {
    Pair<Long, List<ProfileVO>> pageView(Number pageNo, Number pageSize, String createBy);
    ProfileVO detailView(Long id);
    ProfileVO create(ProfileRequest request);
    boolean update(Long id, ProfileRequest request);
    boolean delete(Long id);
}
