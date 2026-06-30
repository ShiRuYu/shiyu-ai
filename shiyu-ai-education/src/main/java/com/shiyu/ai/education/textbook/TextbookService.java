package com.shiyu.ai.education.textbook;

import com.shiyu.ai.dal.dataobject.education.TextbookDO;

import java.util.List;

public interface TextbookService {

    TextbookDO getById(Long id);

    List<TextbookDO> listBySubjectAndGrade(String subjectCode, Integer grade);

    List<TextbookDO> listAll();

    TextbookDO create(TextbookDO textbook);

    void update(TextbookDO textbook);

    void deleteById(Long id);
}
