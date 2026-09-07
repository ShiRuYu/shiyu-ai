package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.WrongQuestionBO;
import com.shiyu.ai.education.port.repository.WrongQuestionRepository;
import com.shiyu.ai.education.dto.WrongQuestionResponse;
import com.shiyu.ai.education.request.WrongQuestionRequest;
import com.shiyu.ai.education.service.WrongQuestionService;
import com.shiyu.ai.kernel.context.ActorContext;
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
    public WrongQuestionResponse getById(ActorContext actor, Long id) {
        WrongQuestionBO bo = wrongQuestionRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, WrongQuestionResponse.class);
    }

    @Override
    public List<WrongQuestionResponse> listByStudentId(ActorContext actor, Long studentId) {
        List<WrongQuestionBO> boList = wrongQuestionRepository.selectByStudentId(actor.tenantId(), studentId);
        return MapstructUtils.convert(boList, WrongQuestionResponse.class);
    }

    @Override
    public WrongQuestionResponse getByStudentAndQuestion(ActorContext actor, Long studentId, Long questionId) {
        WrongQuestionBO bo = wrongQuestionRepository.selectByStudentAndQuestion(actor.tenantId(), studentId, questionId);
        return MapstructUtils.convert(bo, WrongQuestionResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WrongQuestionResponse create(ActorContext actor, WrongQuestionRequest request) {
        WrongQuestionBO bo = new WrongQuestionBO();
        bo.setStudentId(request.getStudentId());
        bo.setQuestionId(request.getQuestionId());
        bo.setKnowledgeId(request.getKnowledgeId());
        bo.setStudentAnswer(request.getStudentAnswer());
        bo.setCorrectTimes(request.getCorrectTimes() == null ? 0 : request.getCorrectTimes());
        bo.setStatus(request.getStatus() == null ? 0 : request.getStatus());
        wrongQuestionRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, WrongQuestionResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, WrongQuestionRequest request) {
        WrongQuestionBO bo = wrongQuestionRepository.selectById(actor.tenantId(), request.getId());
        if (bo != null) {
            bo.setStudentId(request.getStudentId());
            bo.setQuestionId(request.getQuestionId());
            bo.setKnowledgeId(request.getKnowledgeId());
            bo.setStudentAnswer(request.getStudentAnswer());
            bo.setCorrectTimes(request.getCorrectTimes());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            wrongQuestionRepository.update(actor.tenantId(), bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        wrongQuestionRepository.deleteById(actor.tenantId(), id);
    }
}
