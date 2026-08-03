package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.request.IntentDefRequest;
import com.shiyu.ai.agent.vo.IntentDefVO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

/** Application contract for intent-definition administration. */
public interface IntentDefService {
    Pair<Long, List<IntentDefVO>> pageView(Number pageNo, Number pageSize, String agentId, String name, String code, String category);
    IntentDefVO detailView(Long id);
    IntentDefVO create(IntentDefRequest request);
    IntentDefVO update(Long id, IntentDefRequest request);
    void deleteById(Long id);
    void deleteByIds(List<Long> ids);
    List<IdNameOptionVO> listAllOptions();
}
