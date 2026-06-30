package com.shiyu.ai.education.review;

import com.shiyu.ai.dal.dataobject.education.ReviewTaskDO;

import java.util.List;

public interface ReviewService {

    ReviewTaskDO getById(Long id);

    List<ReviewTaskDO> listTodayTasks(Long studentId);

    List<ReviewTaskDO> listByStudentAndStatus(Long studentId, String status);

    List<ReviewTaskDO> listByStudentAndKnowledge(Long studentId, Long knowledgeId);

    ReviewTaskDO create(ReviewTaskDO task);

    void update(ReviewTaskDO task);
}
