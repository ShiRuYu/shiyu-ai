package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.request.AgentRequest;
import com.shiyu.ai.agent.vo.AgentDetailVO;
import com.shiyu.ai.agent.vo.AgentVO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.agent.vo.NodeTypeMetaVO;
import com.shiyu.ai.kernel.context.ActorContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Agent Admin 接口
 */

public interface AgentAdminService {

    /**
     * Get Page
     * @param Number Number
     * @param Number Number
     * @return 处理结果
     */
    Pair<Long, List<AgentVO>> getPage(ActorContext actor, Number pageNo, Number pageSize, String name, Integer status);

    /**
     * Get By Id
     * @return 处理结果
     */
    AgentDetailVO getById(ActorContext actor, Long id);

    /**
     * Create
     * @param AgentRequest AgentRequest
     * @return 处理结果
     */
    AgentVO create(ActorContext actor, AgentRequest request);

    /**
     * Update
     * @param AgentRequest AgentRequest
     * @return 处理结果
     */
    AgentVO update(ActorContext actor, Long id, AgentRequest request);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(ActorContext actor, Long id);

    /**
     * Get Node Types
     * @return 处理结果
     */
    List<NodeTypeMetaVO> getNodeTypes();

    /**
     * List All Options
     * @return 处理结果
     */
    List<IdNameOptionVO> listAllOptions(ActorContext actor);
}
