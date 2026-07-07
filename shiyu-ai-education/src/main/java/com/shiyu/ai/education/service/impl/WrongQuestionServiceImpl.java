package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.dal.dataobject.education.WrongQuestionDO;
import com.shiyu.ai.education.service.WrongQuestionService;
import com.shiyu.ai.dal.repository.education.WrongQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WrongQuestionServiceImpl implements WrongQuestionService {

    private final WrongQuestionRepository wrongQuestionRepository;

    @Override
    public WrongQuestionDO getById(Long id) {
        return wrongQuestionRepository.selectById(id);
    }

    @Override
    public List<WrongQuestionDO> listByStudentId(Long studentId) {
        return wrongQuestionRepository.selectByStudentId(studentId);
    }

    @Override
    public WrongQuestionDO getByStudentAndQuestion(Long studentId, Long questionId) {
        return wrongQuestionRepository.selectByStudentAndQuestion(studentId, questionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WrongQuestionDO create(WrongQuestionDO wrongQuestion) {
        wrongQuestionRepository.insert(wrongQuestion);
        return wrongQuestion;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WrongQuestionDO wrongQuestion) {
        wrongQuestionRepository.update(wrongQuestion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        wrongQuestionRepository.deleteById(id);
    }
}
