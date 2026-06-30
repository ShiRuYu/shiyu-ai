package com.shiyu.ai.education.question.impl;

import com.shiyu.ai.dal.dataobject.education.QuestionDO;
import com.shiyu.ai.education.question.QuestionService;
import com.shiyu.ai.education.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    @Override
    public QuestionDO getById(Long id) {
        return questionRepository.selectById(id);
    }

    @Override
    public QuestionDO getByCode(String code) {
        return questionRepository.selectByCode(code);
    }

    @Override
    public List<QuestionDO> listBySubjectAndGrade(String subjectCode, Integer grade) {
        return questionRepository.selectBySubjectAndGrade(subjectCode, grade);
    }

    @Override
    public List<QuestionDO> listByDifficulty(Integer difficulty) {
        return questionRepository.selectByDifficulty(difficulty);
    }

    @Override
    public List<QuestionDO> listByType(String type) {
        return questionRepository.selectByType(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionDO create(QuestionDO question) {
        questionRepository.insert(question);
        return question;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(QuestionDO question) {
        questionRepository.update(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementUsedCount(Long id) {
        questionRepository.incrementUsedCount(id);
    }
}
