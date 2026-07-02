package com.shiyu.ai.aiagent.service;

import com.shiyu.ai.aiagent.request.AgentRequest;
import com.shiyu.ai.aiagent.vo.AgentDetailVO;
import com.shiyu.ai.aiagent.vo.AgentVO;
import com.shiyu.ai.model.vo.IdNameOptionVO;
import com.shiyu.ai.aiagent.vo.NodeTypeMetaVO;
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
    Pair<Long, List<AgentVO>> getPage(Number pageNo, Number pageSize, String name, String status);

    /**
     * Get By Id
     * @return 处理结果
     */
    AgentDetailVO getById(Long id);

    /**
     * Create
     * @param AgentRequest AgentRequest
     * @return 处理结果
     */
    AgentVO create(AgentRequest request);

    /**
     * Update
     * @param AgentRequest AgentRequest
     * @return 处理结果
     */
    AgentVO update(Long id, AgentRequest request);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);

    /**
     * Get Node Types
     * @return 处理结果
     */
    List<NodeTypeMetaVO> getNodeTypes();

    /**
     * List All Options
     * @return 处理结果
     */
    List<IdNameOptionVO> listAllOptions();
}
