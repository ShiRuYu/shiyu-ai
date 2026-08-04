package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.ReviewTaskBO;
import java.time.LocalDate;
import java.util.List;

public interface ReviewTaskRepository {
    ReviewTaskBO selectById(Long id);
    List<ReviewTaskBO> selectTodayTasks(Long studentId);
    List<ReviewTaskBO> selectByStudentAndStatus(Long studentId, Integer status);
    List<ReviewTaskBO> selectByStudentAndKnowledge(Long studentId, Long knowledgeId);
    int insert(ReviewTaskBO entity);
    int update(ReviewTaskBO entity);
    int deleteById(Long id);
}
