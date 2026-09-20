package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.web.dto.SubjectResponse;
import com.shiyu.ai.education.implementation.web.request.SubjectRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 提供 学科 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface SubjectService {

    /**
     * 根据标识获取目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     *
     * @return 处理结果。
     */
    SubjectResponse getById(ActorContext actor, Long id);

    /**
     * 根据编码获取目标记录。
     *
     * @param actor 调用方上下文。
     * @param code code 参数。
     *
     * @return 处理结果。
     */
    SubjectResponse getByCode(ActorContext actor, String code);

    /**
     * 分页查询学科。
     *
     * @param actor 调用方上下文。
     * @param pageNum 页码。
     * @param pageSize 每页条数。
     *
     * @return 分页查询结果。
     */
    PageData<SubjectResponse> page(ActorContext actor, int pageNum, int pageSize);

    /**
     * 查询 学科 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param gradeLevel 用于完成本次业务处理的 gradeLevel 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<SubjectResponse> listByGradeLevel(ActorContext actor, String gradeLevel);

    /**
     * 创建学科。
     *
     * @param actor 调用方上下文。
     * @param subject 学科请求。
     *
     * @return 处理后的学科。
     */
    SubjectResponse create(ActorContext actor, SubjectRequest subject);

    /**
     * 更新学科。
     *
     * @param actor 调用方上下文。
     * @param subject 学科请求。
     */
    void update(ActorContext actor, SubjectRequest subject);

    /**
     * 根据标识删除目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     */
    void deleteById(ActorContext actor, Long id);
}
