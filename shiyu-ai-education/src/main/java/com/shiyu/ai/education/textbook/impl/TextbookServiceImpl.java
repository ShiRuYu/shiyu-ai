package com.shiyu.ai.education.textbook.impl;

import com.shiyu.ai.dal.dataobject.education.TextbookDO;
import com.shiyu.ai.education.repository.TextbookRepository;
import com.shiyu.ai.education.textbook.TextbookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextbookServiceImpl implements TextbookService {

    private final TextbookRepository textbookRepository;

    @Override
    public TextbookDO getById(Long id) {
        return textbookRepository.selectById(id);
    }

    @Override
    public List<TextbookDO> listBySubjectAndGrade(String subjectCode, Integer grade) {
        return textbookRepository.selectBySubjectAndGrade(subjectCode, grade);
    }

    @Override
    public List<TextbookDO> listAll() {
        return textbookRepository.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TextbookDO create(TextbookDO textbook) {
        textbookRepository.insert(textbook);
        return textbook;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TextbookDO textbook) {
        textbookRepository.update(textbook);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        textbookRepository.deleteById(id);
    }
}
