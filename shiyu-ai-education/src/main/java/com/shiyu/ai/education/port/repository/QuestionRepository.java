package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.QuestionBO;
import java.util.List;

public interface QuestionRepository {
    QuestionBO selectById(Long id);
    PageData<QuestionBO> selectPage(int pageNum, int pageSize);
    List<QuestionBO> selectBySubjectAndGrade(String subjectCode, Integer grade);
    List<QuestionBO> selectByDifficulty(Integer difficulty);
    List<QuestionBO> selectByType(String type);
    QuestionBO selectByCode(String code);
    void incrementUsedCount(Long id);
    List<QuestionBO> selectAll();
    int insert(QuestionBO entity);
    int update(QuestionBO entity);
    int deleteById(Long id);
}
