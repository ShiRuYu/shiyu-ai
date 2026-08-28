package com.shiyu.ai.record.service;

import com.shiyu.ai.record.request.ProfileRequest;
import com.shiyu.ai.record.vo.ProfileVO;
import com.shiyu.ai.kernel.context.ActorContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface ProfileService {
    Pair<Long, List<ProfileVO>> pageView(ActorContext actor, Number pageNo, Number pageSize, String createBy);
    ProfileVO detailView(ActorContext actor, Long id);
    ProfileVO create(ActorContext actor, ProfileRequest request);
    boolean update(ActorContext actor, Long id, ProfileRequest request);
    boolean delete(ActorContext actor, Long id);
}
