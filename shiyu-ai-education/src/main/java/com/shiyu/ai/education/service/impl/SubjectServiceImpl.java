package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.SubjectBO;
import com.shiyu.ai.dal.education.repository.SubjectRepository;
import com.shiyu.ai.education.dto.SubjectResponse;
import com.shiyu.ai.education.request.SubjectRequest;
import com.shiyu.ai.education.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    @Override
    public SubjectResponse getById(Long id) {
        SubjectBO bo = subjectRepository.selectById(id);
        return MapstructUtils.convert(bo, SubjectResponse.class);
    }

    @Override
    public SubjectResponse getByCode(String code) {
        SubjectBO bo = subjectRepository.selectByCode(code);
        return MapstructUtils.convert(bo, SubjectResponse.class);
    }

    @Override
    public List<SubjectResponse> listByGradeLevel(String gradeLevel) {
        List<SubjectBO> boList = subjectRepository.selectByGradeLevel(gradeLevel);
        return MapstructUtils.convert(boList, SubjectResponse.class);
    }

    @Override
    public PageData<SubjectResponse> page(int pageNum, int pageSize) {
        PageData<SubjectBO> boPage = subjectRepository.selectPage(pageNum, pageSize);
        List<SubjectResponse> items = MapstructUtils.convert(boPage.getItems(), SubjectResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubjectResponse create(SubjectRequest request) {
        SubjectBO bo = new SubjectBO();
        bo.setCode(request.getCode());
        bo.setName(request.getName());
        bo.setGradeLevel(request.getGradeLevel());
        bo.setIcon(request.getIcon());
        bo.setSortOrder(request.getSortOrder());
        bo.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        subjectRepository.insert(bo);
        return MapstructUtils.convert(bo, SubjectResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SubjectRequest request) {
        SubjectBO bo = subjectRepository.selectById(request.getId());
        if (bo != null) {
            bo.setCode(request.getCode());
            bo.setName(request.getName());
            bo.setGradeLevel(request.getGradeLevel());
            bo.setIcon(request.getIcon());
            bo.setSortOrder(request.getSortOrder());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            subjectRepository.update(bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        subjectRepository.deleteById(id);
    }
}
