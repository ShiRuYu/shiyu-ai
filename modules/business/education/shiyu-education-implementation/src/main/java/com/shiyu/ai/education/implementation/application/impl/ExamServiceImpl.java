package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.application.ExamService;
import com.shiyu.ai.education.implementation.domain.model.ExamBO;
import com.shiyu.ai.education.implementation.domain.port.repository.ExamRepository;
import com.shiyu.ai.education.implementation.web.dto.ExamResponse;
import com.shiyu.ai.education.implementation.web.dto.SubmitAnswerRequest;
import com.shiyu.ai.education.implementation.web.request.ExamRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@code ExamServiceImpl} 实现教育模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    /**
     * 考试仓储，表示当前对象中的对应属性。
     */
    private final ExamRepository examRepository;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public ExamResponse getById(ActorContext actor, Long id) {
        ExamBO bo = examRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, ExamResponse.class);
    }

    /**
     * {@code listBySubjectCode} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param subjectCode 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<ExamResponse> listBySubjectCode(ActorContext actor, String subjectCode) {
        List<ExamBO> boList = examRepository.selectBySubjectCode(actor.tenantId(), subjectCode);
        return MapstructUtils.convert(boList, ExamResponse.class);
    }

    /**
     * {@code listByTeacherId} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param teacherId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<ExamResponse> listByTeacherId(ActorContext actor, Long teacherId) {
        List<ExamBO> boList = examRepository.selectByTeacherId(actor.tenantId(), teacherId);
        return MapstructUtils.convert(boList, ExamResponse.class);
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
    public PageData<ExamResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<ExamBO> boPage = examRepository.selectPage(actor.tenantId(), pageNum, pageSize);
        List<ExamResponse> items = MapstructUtils.convert(boPage.getItems(), ExamResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    /**
     * {@code submit} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param examId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public ExamResponse submit(ActorContext actor, Long examId, SubmitAnswerRequest request) {
        return getById(actor, examId);
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

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     */
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

    /**
     * {@code deleteById} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        examRepository.deleteById(actor.tenantId(), id);
    }
}
