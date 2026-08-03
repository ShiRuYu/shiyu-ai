package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.CourseBO;
import java.util.List;

public interface CourseRepository {
    CourseBO selectById(Long id);
    PageData<CourseBO> selectPage(int pageNum, int pageSize);
    List<CourseBO> selectBySubjectCode(String subjectCode);
    List<CourseBO> selectByGrade(Integer grade);
    List<CourseBO> selectAll();
    int insert(CourseBO entity);
    int update(CourseBO entity);
    int deleteById(Long id);
}
