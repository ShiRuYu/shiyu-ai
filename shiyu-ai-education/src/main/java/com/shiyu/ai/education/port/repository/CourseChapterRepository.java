package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.education.domain.model.CourseChapterBO;
import java.util.List;

public interface CourseChapterRepository {
    List<CourseChapterBO> selectByCourseId(Long courseId);
}
