package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.iam.implementation.request.SetTimezoneRequest;
import com.shiyu.ai.iam.implementation.vo.TimezoneOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * TimezoneService 服务接口，负责执行身份与访问领域相关业务操作。
 */
public interface TimezoneService {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @return 符合条件的结果集合。
     */
    List<TimezoneOptionVO> getTimezoneOptions();

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     *
     * @return 操作结果。
     */
    String getTimezone(ActorContext actor);

    /**
     * 执行 {@code setTimezone} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 条件是否满足。
     */
    boolean setTimezone(ActorContext actor, SetTimezoneRequest request);
}
