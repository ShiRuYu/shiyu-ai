package com.shiyu.ai.education.service;


import java.util.List;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.dto.SubjectResponse;
import com.shiyu.ai.education.request.SubjectRequest;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * Subject 接口
 */

public interface SubjectService {

    /**
     * Get By Id
     * @return 处理结果
     */
    SubjectResponse getById(ActorContext actor, Long id);

    /**
     * Get By Code
     * @return 处理结果
     */
    SubjectResponse getByCode(ActorContext actor, String code);

    /**
     * List All
     * @return 处理结果
     */

    PageData<SubjectResponse> page(ActorContext actor, int pageNum, int pageSize);

    /**
     * List By Grade Level
     * @return 处理结果
     */
    List<SubjectResponse> listByGradeLevel(ActorContext actor, String gradeLevel);

    /**
     * Create
     * @param SubjectResponse SubjectDO
     * @return 处理结果
     */
    SubjectResponse create(ActorContext actor, SubjectRequest subject);

    /**
     * Update
     * @param SubjectResponse SubjectDO
     * @return 处理结果
     */
    void update(ActorContext actor, SubjectRequest subject);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(ActorContext actor, Long id);
}
