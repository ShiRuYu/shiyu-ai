package com.shiyu.ai.agent.implementation.service;

import com.shiyu.ai.agent.implementation.request.AgentRequest;
import com.shiyu.ai.agent.implementation.vo.AgentDetailVO;
import com.shiyu.ai.agent.implementation.vo.AgentVO;
import com.shiyu.ai.agent.implementation.vo.NodeTypeMetaVO;
import com.shiyu.ai.common.foundation.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 提供 智能体 Admin 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface AgentAdminService {

    /**
     * 查询 智能体 Admin 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param status 用于完成本次业务处理的 status 参数。
     * @return 返回总数及当前页数据，左值为总数，右值为数据列表。
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
     * 查询 智能体 Admin 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<NodeTypeMetaVO> getNodeTypes();

    /**
     * 查询 智能体 Admin 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<IdNameOptionVO> listAllOptions(ActorContext actor);
}
