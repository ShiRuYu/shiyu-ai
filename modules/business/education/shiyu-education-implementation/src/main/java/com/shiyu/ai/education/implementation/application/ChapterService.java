package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.web.dto.ChapterResponse;
import com.shiyu.ai.education.implementation.web.request.ChapterRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * ChapterService 服务接口，负责执行教育领域相关业务操作。
 */
public interface ChapterService {

    /**
     * 根据标识查询对应的数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    ChapterResponse getById(ActorContext actor, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param textbookId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<ChapterResponse> listByTextbookId(ActorContext actor, Long textbookId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param textbookId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<ChapterResponse> listRootChapters(ActorContext actor, Long textbookId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param parentId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<ChapterResponse> listByParentId(ActorContext actor, Long parentId);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 操作结果。
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
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param chapterId 章节标识。
     * @param knowledgeIds 方法参数。
     */
    void replaceKnowledgeIds(ActorContext actor, Long chapterId, List<Long> knowledgeIds);
}
