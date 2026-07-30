package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.StudentBO;
import com.shiyu.ai.dal.education.repository.StudentRepository;
import com.shiyu.ai.education.dto.StudentResponse;
import com.shiyu.ai.education.request.StudentRequest;
import com.shiyu.ai.education.service.StudentService;
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
    public StudentResponse getById(Long id) {
        StudentBO bo = studentRepository.selectById(id);
        return MapstructUtils.convert(bo, StudentResponse.class);
    }

    @Override
    public StudentResponse getByUserId(Long userId) {
        StudentBO bo = studentRepository.selectByUserId(userId);
        return MapstructUtils.convert(bo, StudentResponse.class);
    }

    @Override
    public PageData<StudentResponse> page(int pageNum, int pageSize) {
        PageData<StudentBO> boPage = studentRepository.selectPage(pageNum, pageSize);
        List<StudentResponse> items = MapstructUtils.convert(boPage.getItems(), StudentResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentResponse create(StudentRequest request) {
        StudentBO bo = new StudentBO();
        bo.setName(request.getName());
        bo.setUserId(request.getUserId());
        bo.setStudentNo(request.getStudentNo());
        bo.setGrade(request.getGrade());
        bo.setGradeLevel(request.getGradeLevel());
        bo.setSchool(request.getSchool());
        bo.setClassName(request.getClassName());
        bo.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        studentRepository.insert(bo);
        return MapstructUtils.convert(bo, StudentResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(StudentRequest request) {
        StudentBO bo = studentRepository.selectById(request.getId());
        if (bo != null) {
            bo.setName(request.getName());
            bo.setUserId(request.getUserId());
            bo.setStudentNo(request.getStudentNo());
            bo.setGrade(request.getGrade());
            bo.setGradeLevel(request.getGradeLevel());
            bo.setSchool(request.getSchool());
            bo.setClassName(request.getClassName());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            studentRepository.update(bo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }
}
