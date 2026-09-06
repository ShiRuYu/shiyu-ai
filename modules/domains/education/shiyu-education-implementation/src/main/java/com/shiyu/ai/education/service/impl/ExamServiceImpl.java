package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.ExamBO;
import com.shiyu.ai.education.port.repository.ExamRepository;
import com.shiyu.ai.education.dto.ExamResponse;
import com.shiyu.ai.education.dto.SubmitAnswerRequest;
import com.shiyu.ai.education.request.ExamRequest;
import com.shiyu.ai.education.service.ExamService;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;

    @Override
    public ExamResponse getById(ActorContext actor, Long id) {
        ExamBO bo = examRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, ExamResponse.class);
    }

    @Override
    public List<ExamResponse> listBySubjectCode(ActorContext actor, String subjectCode) {
        List<ExamBO> boList = examRepository.selectBySubjectCode(actor.tenantId(), subjectCode);
        return MapstructUtils.convert(boList, ExamResponse.class);
    }

    @Override
    public List<ExamResponse> listByTeacherId(ActorContext actor, Long teacherId) {
        List<ExamBO> boList = examRepository.selectByTeacherId(actor.tenantId(), teacherId);
        return MapstructUtils.convert(boList, ExamResponse.class);
    }

    @Override
    public PageData<ExamResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<ExamBO> boPage = examRepository.selectPage(actor.tenantId(), pageNum, pageSize);
        List<ExamResponse> items = MapstructUtils.convert(boPage.getItems(), ExamResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    @Override
    public ExamResponse submit(ActorContext actor, Long examId, SubmitAnswerRequest request) {
        return getById(actor, examId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamResponse create(ActorContext actor, ExamRequest request) {
        ExamBO bo = new ExamBO();
        bo.setName(request.getName());
        bo.setType(request.getType());
        bo.setSubjectCode(request.getSubjectCode());
        bo.setGrade(request.getGrade());
        bo.setTeacherId(request.getTeacherId());
        bo.setDurationMin(request.getDurationMin());
        bo.setTotalScore(request.getTotalScore());
        bo.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        examRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, ExamResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, ExamRequest request) {
        ExamBO bo = examRepository.selectById(actor.tenantId(), request.getId());
        if (bo != null) {
            bo.setName(request.getName());
            bo.setType(request.getType());
            bo.setSubjectCode(request.getSubjectCode());
            bo.setGrade(request.getGrade());
            bo.setTeacherId(request.getTeacherId());
            bo.setDurationMin(request.getDurationMin());
            bo.setTotalScore(request.getTotalScore());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            examRepository.update(actor.tenantId(), bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        examRepository.deleteById(actor.tenantId(), id);
    }
}
