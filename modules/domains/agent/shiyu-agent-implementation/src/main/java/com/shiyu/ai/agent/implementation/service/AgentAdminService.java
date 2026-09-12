package com.shiyu.ai.agent.implementation.service;

import com.shiyu.ai.agent.implementation.request.AgentRequest;
import com.shiyu.ai.agent.implementation.vo.AgentDetailVO;
import com.shiyu.ai.agent.implementation.vo.AgentVO;
import com.shiyu.ai.agent.implementation.vo.NodeTypeMetaVO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * AgentAdminService 服务接口，负责执行智能体领域相关业务操作。
 */
public interface AgentAdminService {

    /**
     * 获取page。
     *
     * @param actor 调用方上下文。
     * @param pageNo pageNo 参数。
     * @param pageSize 每页条数。
     * @param name 名称。
     * @param status 状态。
     *
     * @return 结果列表。
     */
    Pair<Long, List<AgentVO>> getPage(
            ActorContext actor, Number pageNo, Number pageSize, String name, Integer status);

    /**
     * 根据标识获取目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     *
     * @return 处理结果。
     */
    AgentDetailVO getById(ActorContext actor, Long id);

    /**
     * 创建智能体管理数据。
     *
     * @param actor 调用方上下文。
     * @param request 请求对象。
     *
     * @return 处理后的智能体管理数据。
     */
    AgentVO create(ActorContext actor, AgentRequest request);

    /**
     * 更新智能体管理数据。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     * @param request 请求对象。
     *
     * @return 处理结果。
     */
    AgentVO update(ActorContext actor, Long id, AgentRequest request);

    /**
     * 根据标识删除目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     */
    void deleteById(ActorContext actor, Long id);

    /**
     * 获取节点types。
     *
     * @return 结果列表。
     */
    List<NodeTypeMetaVO> getNodeTypes();

    /**
     * 查询全部选项列表。
     *
     * @param actor 调用方上下文。
     *
     * @return 结果列表。
     */
    List<IdNameOptionVO> listAllOptions(ActorContext actor);
}
