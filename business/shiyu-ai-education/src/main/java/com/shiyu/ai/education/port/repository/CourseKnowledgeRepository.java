package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.education.domain.model.CourseKnowledgeBO;
import java.util.List;

public interface CourseKnowledgeRepository {
    List<CourseKnowledgeBO> selectByCourseId(Long courseId);
}
