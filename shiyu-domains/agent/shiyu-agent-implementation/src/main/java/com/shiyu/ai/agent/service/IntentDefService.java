package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.request.IntentDefRequest;
import com.shiyu.ai.agent.vo.IntentDefVO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import org.apache.commons.lang3.tuple.Pair;
import com.shiyu.ai.kernel.context.ActorContext;
import java.util.List;

/** Application contract for intent-definition administration. */
public interface IntentDefService {
    Pair<Long, List<IntentDefVO>> pageView(ActorContext actor, Number pageNo, Number pageSize, String agentId, String name, String code, String category);
    IntentDefVO detailView(ActorContext actor, Long id);
    IntentDefVO create(ActorContext actor, IntentDefRequest request);
    IntentDefVO update(ActorContext actor, Long id, IntentDefRequest request);
    void deleteById(ActorContext actor, Long id);
    void deleteByIds(ActorContext actor, List<Long> ids);
    List<IdNameOptionVO> listAllOptions(ActorContext actor);
}
