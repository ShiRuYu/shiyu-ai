package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.iam.implementation.request.SetTimezoneRequest;
import com.shiyu.ai.iam.implementation.vo.TimezoneOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 提供 Timezone 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface TimezoneService {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @return 符合条件的结果集合。
     */
    List<TimezoneOptionVO> getTimezoneOptions();

    /**
     * 查询 Timezone 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回 Timezone 相关操作生成的结果数据。
     */
    String getTimezone(ActorContext actor);

    /**
     * 更新或设置 Timezone 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回本次条件判断是否成立。
     */
    boolean setTimezone(ActorContext actor, SetTimezoneRequest request);
}
