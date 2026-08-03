package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.WrongQuestionBO;
import java.util.List;

public interface WrongQuestionRepository {
    WrongQuestionBO selectById(Long id);
    List<WrongQuestionBO> selectByStudentId(Long studentId);
    WrongQuestionBO selectByStudentAndQuestion(Long studentId, Long questionId);
    int insert(WrongQuestionBO entity);
    int update(WrongQuestionBO entity);
    int deleteById(Long id);
}
