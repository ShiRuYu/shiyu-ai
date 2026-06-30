package com.shiyu.ai.education.question;

import com.shiyu.ai.dal.dataobject.education.QuestionDO;

import java.util.List;

public interface QuestionService {

    QuestionDO getById(Long id);

    QuestionDO getByCode(String code);

    List<QuestionDO> listBySubjectAndGrade(String subjectCode, Integer grade);

    List<QuestionDO> listByDifficulty(Integer difficulty);

    List<QuestionDO> listByType(String type);

    QuestionDO create(QuestionDO question);

    void update(QuestionDO question);

    void deleteById(Long id);

    void incrementUsedCount(Long id);
}
