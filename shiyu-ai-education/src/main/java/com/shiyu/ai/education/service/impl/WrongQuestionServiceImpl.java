package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.bo.education.WrongQuestionBO;
import com.shiyu.ai.dal.repository.education.WrongQuestionRepository;
import com.shiyu.ai.education.dto.WrongQuestionResponse;
import com.shiyu.ai.education.request.WrongQuestionRequest;
import com.shiyu.ai.education.service.WrongQuestionService;
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
    public WrongQuestionResponse getById(Long id) {
        WrongQuestionBO bo = wrongQuestionRepository.selectById(id);
        return MapstructUtils.convert(bo, WrongQuestionResponse.class);
    }

    @Override
    public List<WrongQuestionResponse> listByStudentId(Long studentId) {
        List<WrongQuestionBO> boList = wrongQuestionRepository.selectByStudentId(studentId);
        return MapstructUtils.convert(boList, WrongQuestionResponse.class);
    }

    @Override
    public WrongQuestionResponse getByStudentAndQuestion(Long studentId, Long questionId) {
        WrongQuestionBO bo = wrongQuestionRepository.selectByStudentAndQuestion(studentId, questionId);
        return MapstructUtils.convert(bo, WrongQuestionResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WrongQuestionResponse create(WrongQuestionRequest request) {
        WrongQuestionBO bo = new WrongQuestionBO();
        bo.setStudentId(request.getStudentId());
        bo.setQuestionId(request.getQuestionId());
        bo.setKnowledgeId(request.getKnowledgeId());
        bo.setStudentAnswer(request.getStudentAnswer());
        wrongQuestionRepository.insert(bo);
        return MapstructUtils.convert(bo, WrongQuestionResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WrongQuestionRequest request) {
        WrongQuestionBO bo = wrongQuestionRepository.selectById(request.getId());
        if (bo != null) {
            bo.setStudentId(request.getStudentId());
            bo.setQuestionId(request.getQuestionId());
            bo.setKnowledgeId(request.getKnowledgeId());
            bo.setStudentAnswer(request.getStudentAnswer());
            wrongQuestionRepository.update(bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        wrongQuestionRepository.deleteById(id);
    }
}
