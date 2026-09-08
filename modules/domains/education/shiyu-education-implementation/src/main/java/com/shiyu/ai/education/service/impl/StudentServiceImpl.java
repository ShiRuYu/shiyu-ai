package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.StudentBO;
import com.shiyu.ai.education.port.repository.StudentRepository;
import com.shiyu.ai.education.dto.StudentResponse;
import com.shiyu.ai.education.request.StudentRequest;
import com.shiyu.ai.education.service.StudentService;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    public StudentResponse getById(ActorContext actor, Long id) {
        StudentBO bo = studentRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, StudentResponse.class);
    }

    @Override
    public StudentResponse getByUserId(ActorContext actor, Long userId) {
        StudentBO bo = studentRepository.selectByUserId(actor.tenantId(), userId);
        return MapstructUtils.convert(bo, StudentResponse.class);
    }

    @Override
    public PageData<StudentResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<StudentBO> boPage = studentRepository.selectPage(actor.tenantId(), pageNum, pageSize);
        List<StudentResponse> items = MapstructUtils.convert(boPage.getItems(), StudentResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentResponse create(ActorContext actor, StudentRequest request) {
        StudentBO bo = new StudentBO();
        bo.setTenantId(actor.tenantId().value());
        bo.setName(request.getName());
        bo.setUserId(request.getUserId());
        bo.setStudentNo(request.getStudentNo());
        bo.setGrade(request.getGrade());
        bo.setGradeLevel(request.getGradeLevel());
        bo.setSchool(request.getSchool());
        bo.setClassName(request.getClassName());
        bo.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        studentRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, StudentResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, StudentRequest request) {
        StudentBO bo = studentRepository.selectById(actor.tenantId(), request.getId());
        if (bo != null) {
            bo.setName(request.getName());
            bo.setUserId(request.getUserId());
            bo.setStudentNo(request.getStudentNo());
            bo.setGrade(request.getGrade());
            bo.setGradeLevel(request.getGradeLevel());
            bo.setSchool(request.getSchool());
            bo.setClassName(request.getClassName());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            studentRepository.update(actor.tenantId(), bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        studentRepository.deleteById(actor.tenantId(), id);
    }
}
