package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.TextbookBO;
import java.util.List;

public interface TextbookRepository {
    TextbookBO selectById(Long id);
    PageData<TextbookBO> selectPage(int pageNum, int pageSize);
    List<TextbookBO> selectBySubjectAndGrade(String subjectCode, Integer grade);
    List<TextbookBO> selectAll();
    int insert(TextbookBO entity);
    int update(TextbookBO entity);
    int deleteById(Long id);
}
