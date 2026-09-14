package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.application.StudentService;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;
import com.shiyu.ai.education.implementation.domain.port.repository.StudentRepository;
import com.shiyu.ai.education.implementation.web.dto.StudentResponse;
import com.shiyu.ai.education.implementation.web.request.StudentRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@code StudentServiceImpl} 实现教育模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    /**
     * 学生仓储，表示当前对象中的对应属性。
     */
    private final StudentRepository studentRepository;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public StudentResponse getById(ActorContext actor, Long id) {
        StudentBO bo = studentRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, StudentResponse.class);
    }

    /**
     * {@code getByUserId} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param userId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public StudentResponse getByUserId(ActorContext actor, Long userId) {
        StudentBO bo = studentRepository.selectByUserId(actor.tenantId(), userId);
        return MapstructUtils.convert(bo, StudentResponse.class);
    }

    /**
     * {@code page} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public PageData<StudentResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<StudentBO> boPage =
                studentRepository.selectPage(actor.tenantId(), pageNum, pageSize);
        List<StudentResponse> items =
                MapstructUtils.convert(boPage.getItems(), StudentResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     */
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

    /**
     * {@code deleteById} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        studentRepository.deleteById(actor.tenantId(), id);
    }
}
