package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.web.dto.ChapterResponse;
import com.shiyu.ai.education.implementation.web.request.ChapterRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 提供 章节 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface ChapterService {

    /**
     * 查询 章节 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 章节 相关操作生成的结果数据。
     */
    ChapterResponse getById(ActorContext actor, Long id);

    /**
     * 查询 章节 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param textbookId 用于定位textbook的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ChapterResponse> listByTextbookId(ActorContext actor, Long textbookId);

    /**
     * 查询 章节 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param textbookId 用于定位textbook的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ChapterResponse> listRootChapters(ActorContext actor, Long textbookId);

    /**
     * 查询 章节 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param parentId 用于定位parent的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ChapterResponse> listByParentId(ActorContext actor, Long parentId);

    /**
     * 创建或保存 章节 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 章节 相关操作生成的结果数据。
     */
    ChapterResponse create(ActorContext actor, ChapterRequest request);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param request 请求参数。
     */
    void update(ActorContext actor, Long id, ChapterRequest request);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     */
    void delete(ActorContext actor, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param chapterId 章节标识。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> listKnowledgeIds(ActorContext actor, Long chapterId);

    /**
     * 执行 章节 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param chapterId 用于定位chapter的标识。
     * @param knowledgeIds 待处理的业务对象标识集合。
     */
    void replaceKnowledgeIds(ActorContext actor, Long chapterId, List<Long> knowledgeIds);
}
