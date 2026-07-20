package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.ExamBO;
import com.shiyu.ai.dal.education.repository.ExamRepository;
import com.shiyu.ai.education.dto.ExamResponse;
import com.shiyu.ai.education.dto.SubmitAnswerRequest;
import com.shiyu.ai.education.request.ExamRequest;
import com.shiyu.ai.education.service.ExamService;
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
    public ExamResponse getById(Long id) {
        ExamBO bo = examRepository.selectById(id);
        return MapstructUtils.convert(bo, ExamResponse.class);
    }

    @Override
    public List<ExamResponse> listBySubjectCode(String subjectCode) {
        List<ExamBO> boList = examRepository.selectBySubjectCode(subjectCode);
        return MapstructUtils.convert(boList, ExamResponse.class);
    }

    @Override
    public List<ExamResponse> listByTeacherId(Long teacherId) {
        List<ExamBO> boList = examRepository.selectByTeacherId(teacherId);
        return MapstructUtils.convert(boList, ExamResponse.class);
    }

    @Override
    public PageData<ExamResponse> page(int pageNum, int pageSize) {
        PageData<ExamBO> boPage = examRepository.selectPage(pageNum, pageSize);
        List<ExamResponse> items = MapstructUtils.convert(boPage.getItems(), ExamResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    @Override
    public ExamResponse submit(Long examId, SubmitAnswerRequest request) {
        return getById(examId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamResponse create(ExamRequest request) {
        ExamBO bo = new ExamBO();
        bo.setName(request.getName());
        bo.setType(request.getType());
        bo.setSubjectCode(request.getSubjectCode());
        bo.setGrade(request.getGrade());
        bo.setDurationMin(request.getDurationMin());
        bo.setTotalScore(request.getTotalScore());
        examRepository.insert(bo);
        return MapstructUtils.convert(bo, ExamResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ExamRequest request) {
        ExamBO bo = examRepository.selectById(request.getId());
        if (bo != null) {
            bo.setName(request.getName());
            bo.setType(request.getType());
            bo.setSubjectCode(request.getSubjectCode());
            bo.setGrade(request.getGrade());
            bo.setDurationMin(request.getDurationMin());
            bo.setTotalScore(request.getTotalScore());
            examRepository.update(bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        examRepository.deleteById(id);
    }
}
