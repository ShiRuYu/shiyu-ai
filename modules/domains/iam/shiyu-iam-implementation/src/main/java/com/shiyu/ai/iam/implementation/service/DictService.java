package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.iam.implementation.request.DictRequest;
import com.shiyu.ai.iam.implementation.vo.DictVO;
import com.shiyu.ai.kernel.context.ActorContext;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 提供 Dict 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface DictService {
    /**
     * 查询 Dict 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @return 返回总数及当前页数据，左值为总数，右值为数据列表。
     */
    Pair<Long, List<DictVO>> pageView(ActorContext actor, Number pageNo, Number pageSize);

    /**
     * 查询 Dict 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param dictType 用于完成本次业务处理的 dictType 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<DictVO> byTypeView(ActorContext actor, String dictType);

    /**
     * 创建或保存 Dict 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 Dict 相关操作生成的结果数据。
     */
    DictVO create(ActorContext actor, DictRequest request);

    /**
     * 更新或设置 Dict 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 Dict 相关操作生成的结果数据。
     */
    DictVO update(ActorContext actor, Long id, DictRequest request);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     */
    void deleteById(ActorContext actor, Long id);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param ids 目标对象标识集合。
     */
    void deleteByIds(ActorContext actor, List<Long> ids);
}
