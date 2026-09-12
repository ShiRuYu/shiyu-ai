package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.web.dto.SubjectResponse;
import com.shiyu.ai.education.implementation.web.request.SubjectRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * SubjectService 服务接口，负责执行教育领域相关业务操作。
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
     * 根据年级等级查询学科列表。
     *
     * @param actor 调用方上下文。
     * @param gradeLevel gradeLevel 参数。
     *
     * @return 结果列表。
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
