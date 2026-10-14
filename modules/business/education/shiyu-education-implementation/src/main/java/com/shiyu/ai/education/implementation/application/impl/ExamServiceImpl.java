package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.application.ExamService;
import com.shiyu.ai.education.implementation.domain.model.ExamBO;
import com.shiyu.ai.education.implementation.domain.port.repository.ExamRepository;
import com.shiyu.ai.education.implementation.web.dto.ExamResponse;
import com.shiyu.ai.education.implementation.web.request.ExamRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 提供 考试 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 查询 考试 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 考试 相关操作生成的结果数据。
     */
    @Override
    public ExamResponse getById(ActorContext actor, Long id) {
        ExamBO bo = examRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, ExamResponse.class);
    }

    /**
     * 查询 考试 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param subjectCode 用于完成本次业务处理的 subjectCode 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ExamResponse> listBySubjectCode(ActorContext actor, String subjectCode) {
        List<ExamBO> boList = examRepository.selectBySubjectCode(actor.tenantId(), subjectCode);
        return MapstructUtils.convert(boList, ExamResponse.class);
    }

    /**
     * 查询 考试 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param teacherId 用于定位teacher的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ExamResponse> listByTeacherId(ActorContext actor, Long teacherId) {
        List<ExamBO> boList = examRepository.selectByTeacherId(actor.tenantId(), teacherId);
        return MapstructUtils.convert(boList, ExamResponse.class);
    }

    /**
     * 查询 考试 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @return 返回 考试 相关操作生成的结果数据。
     */
    @Override
    public PageData<ExamResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<ExamBO> boPage = examRepository.selectPage(actor.tenantId(), pageNum, pageSize);
        List<ExamResponse> items = MapstructUtils.convert(boPage.getItems(), ExamResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    /**
     * 执行 考试 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
        bo.setStatus(0);
        bo.setStartTime(request.getStartTime());
        bo.setEndTime(request.getEndTime());
        examRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, ExamResponse.class);
    }

    /**
     * 执行 考试 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, ExamRequest request) {
        ExamBO bo = examRepository.selectById(actor.tenantId(), request.getId());
        if (bo != null) {
            if (!Integer.valueOf(0).equals(bo.getStatus())) {
                throw new ServiceException("只能编辑草稿考试");
            }
            bo.setName(request.getName());
            bo.setType(request.getType());
            bo.setSubjectCode(request.getSubjectCode());
            bo.setGrade(request.getGrade());
            bo.setTeacherId(request.getTeacherId());
            bo.setDurationMin(request.getDurationMin());
            bo.setTotalScore(request.getTotalScore());
            bo.setStartTime(request.getStartTime());
            bo.setEndTime(request.getEndTime());
            examRepository.update(actor.tenantId(), bo);
        }
    }

    /**
     * 执行 考试 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        ExamBO exam = examRepository.selectById(actor.tenantId(), id);
        if (exam == null || !Integer.valueOf(0).equals(exam.getStatus())) {
            throw new ServiceException("只能删除草稿考试");
        }
        examRepository.deleteById(actor.tenantId(), id);
    }
}
