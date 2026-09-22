package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
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
 * 提供 学生 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 查询 学生 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 学生 相关操作生成的结果数据。
     */
    @Override
    public StudentResponse getById(ActorContext actor, Long id) {
        StudentBO bo = studentRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, StudentResponse.class);
    }

    /**
     * 查询 学生 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param userId 当前操作涉及的用户标识。
     * @return 返回 学生 相关操作生成的结果数据。
     */
    @Override
    public StudentResponse getByUserId(ActorContext actor, Long userId) {
        StudentBO bo = studentRepository.selectByUserId(actor.tenantId(), userId);
        return MapstructUtils.convert(bo, StudentResponse.class);
    }

    /**
     * 查询 学生 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @return 返回 学生 相关操作生成的结果数据。
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
     * 执行 学生 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
     * 执行 学生 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
     * 执行 学生 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        studentRepository.deleteById(actor.tenantId(), id);
    }
}
