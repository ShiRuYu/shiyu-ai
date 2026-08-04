package com.shiyu.ai.agent.port.repository;

import com.shiyu.ai.agent.domain.model.IntentDefBO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface IntentDefRepository {
    Pair<Long, List<IntentDefBO>> selectPage(Number pageNo, Number pageSize, String agentId, String name, String code, String category);
    List<IntentDefBO> selectByAgentId(String agentId);
    List<IntentDefBO> selectByCategory(String agentId, String category);
    IntentDefBO selectById(Long id);
    IntentDefBO create(IntentDefBO bo);
    IntentDefBO update(IntentDefBO bo);
    void deleteById(Long id);
    void deleteByIds(List<Long> ids);
    List<IdNameOptionVO> selectAllOptions();
}
