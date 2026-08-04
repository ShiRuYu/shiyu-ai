package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.SubjectBO;
import java.util.List;

public interface SubjectRepository {
    SubjectBO selectById(Long id);
    SubjectBO selectByCode(String code);
    PageData<SubjectBO> selectPage(int pageNum, int pageSize);
    List<SubjectBO> selectByGradeLevel(String gradeLevel);
    List<SubjectBO> selectAll();
    int insert(SubjectBO entity);
    int update(SubjectBO entity);
    int deleteById(Long id);
}
