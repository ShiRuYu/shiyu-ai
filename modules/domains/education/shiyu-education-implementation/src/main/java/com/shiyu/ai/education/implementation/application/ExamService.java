package com.shiyu.ai.education.implementation.application;


import java.util.List;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.implementation.web.dto.ExamResponse;
import com.shiyu.ai.education.implementation.web.request.ExamRequest;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * Exam 接口
 */

public interface ExamService {

    /**
     * List All
     */

    PageData<ExamResponse> page(ActorContext actor, int pageNum, int pageSize);

    /**
     * Get By Id
     * @return 处理结果
     */
    ExamResponse getById(ActorContext actor, Long id);

    /**
     * List By Subject Code
     * @return 处理结果
     */
    List<ExamResponse> listBySubjectCode(ActorContext actor, String subjectCode);

    /**
     * List By Teacher Id
     * @return 处理结果
     */
    List<ExamResponse> listByTeacherId(ActorContext actor, Long teacherId);

    /**
     * Create
     * @param ExamResponse ExamDO
     * @return 处理结果
     */
    ExamResponse create(ActorContext actor, ExamRequest exam);

    /**
     * Update
     * @param ExamResponse ExamDO
     * @return 处理结果
     */
    void update(ActorContext actor, ExamRequest exam);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(ActorContext actor, Long id);

    /**
     * Submit exam answer
     */
    ExamResponse submit(ActorContext actor, Long id, com.shiyu.ai.education.implementation.web.dto.SubmitAnswerRequest request);
}

