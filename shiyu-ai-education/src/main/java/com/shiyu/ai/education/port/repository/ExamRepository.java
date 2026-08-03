package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.ExamBO;
import java.util.List;

public interface ExamRepository {
    ExamBO selectById(Long id);
    PageData<ExamBO> selectPage(int pageNum, int pageSize);
    List<ExamBO> selectBySubjectCode(String subjectCode);
    List<ExamBO> selectByTeacherId(Long teacherId);
    List<ExamBO> selectAll();
    int insert(ExamBO entity);
    int update(ExamBO entity);
    int deleteById(Long id);
}
