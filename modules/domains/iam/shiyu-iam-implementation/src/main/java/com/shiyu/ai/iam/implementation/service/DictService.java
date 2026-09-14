package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.iam.implementation.request.DictRequest;
import com.shiyu.ai.iam.implementation.vo.DictVO;
import com.shiyu.ai.kernel.context.ActorContext;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * DictService 服务接口，负责执行身份与访问领域相关业务操作。
 */
public interface DictService {
    /**
     * 执行 {@code pageView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     *
     * @return 符合条件的结果集合。
     */
    Pair<Long, List<DictVO>> pageView(ActorContext actor, Number pageNo, Number pageSize);

    /**
     * 执行 {@code byTypeView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param dictType 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<DictVO> byTypeView(ActorContext actor, String dictType);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    DictVO create(ActorContext actor, DictRequest request);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param request 请求参数。
     *
     * @return 操作结果。
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
