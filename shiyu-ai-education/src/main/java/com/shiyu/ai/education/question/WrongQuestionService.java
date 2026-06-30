package com.shiyu.ai.education.question;

import com.shiyu.ai.dal.dataobject.education.WrongQuestionDO;

import java.util.List;

public interface WrongQuestionService {

    WrongQuestionDO getById(Long id);

    List<WrongQuestionDO> listByStudentId(Long studentId);

    WrongQuestionDO getByStudentAndQuestion(Long studentId, Long questionId);

    WrongQuestionDO create(WrongQuestionDO wrongQuestion);

    void update(WrongQuestionDO wrongQuestion);

    void deleteById(Long id);
}
