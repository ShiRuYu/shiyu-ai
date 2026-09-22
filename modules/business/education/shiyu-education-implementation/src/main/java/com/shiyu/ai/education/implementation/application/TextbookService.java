package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.education.implementation.web.dto.TextbookResponse;
import com.shiyu.ai.education.implementation.web.request.TextbookRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 提供 教材 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface TextbookService {

    /**
     * 根据标识获取目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     *
     * @return 处理结果。
     */
    TextbookResponse getById(ActorContext actor, Long id);

    /**
     * 查询 教材 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param subjectCode 用于完成本次业务处理的 subjectCode 参数。
     * @param grade 用于完成本次业务处理的 grade 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<TextbookResponse> listBySubjectAndGrade(
            ActorContext actor, String subjectCode, Integer grade);

    /**
     * 分页查询教材。
     *
     * @param actor 调用方上下文。
     * @param pageNum 页码。
     * @param pageSize 每页条数。
     *
     * @return 分页查询结果。
     */
    PageData<TextbookResponse> page(ActorContext actor, int pageNum, int pageSize);

    /**
     * 创建教材。
     *
     * @param actor 调用方上下文。
     * @param textbook 教材请求。
     *
     * @return 处理后的教材。
     */
    TextbookResponse create(ActorContext actor, TextbookRequest textbook);

    /**
     * 更新教材。
     *
     * @param actor 调用方上下文。
     * @param textbook 教材请求。
     */
    void update(ActorContext actor, TextbookRequest textbook);

    /**
     * 根据标识删除目标记录。
     *
     * @param actor 调用方上下文。
     * @param id 目标对象标识。
     */
    void deleteById(ActorContext actor, Long id);
}
