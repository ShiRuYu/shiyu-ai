package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.StudentBO;
import java.util.List;

public interface StudentRepository {
    StudentBO selectById(Long id);
    StudentBO selectByUserId(Long userId);
    PageData<StudentBO> selectPage(int pageNum, int pageSize);
    List<StudentBO> selectAll();
    int insert(StudentBO entity);
    int update(StudentBO entity);
    int deleteById(Long id);
}
