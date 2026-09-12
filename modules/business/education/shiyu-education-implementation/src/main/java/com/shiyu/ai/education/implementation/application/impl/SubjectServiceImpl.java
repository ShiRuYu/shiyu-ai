package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.application.SubjectService;
import com.shiyu.ai.education.implementation.domain.model.SubjectBO;
import com.shiyu.ai.education.implementation.domain.port.repository.SubjectRepository;
import com.shiyu.ai.education.implementation.web.dto.SubjectResponse;
import com.shiyu.ai.education.implementation.web.request.SubjectRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@code SubjectServiceImpl} 实现教育模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    /**
     * 学科仓储，表示当前对象中的对应属性。
     */
    private final SubjectRepository subjectRepository;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public SubjectResponse getById(ActorContext actor, Long id) {
        SubjectBO bo = subjectRepository.selectById(requireActor(actor).tenantId(), id);
        return MapstructUtils.convert(bo, SubjectResponse.class);
    }

    /**
     * {@code getByCode} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public SubjectResponse getByCode(ActorContext actor, String code) {
        SubjectBO bo = subjectRepository.selectByCode(requireActor(actor).tenantId(), code);
        return MapstructUtils.convert(bo, SubjectResponse.class);
    }

    /**
     * {@code listByGradeLevel} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param gradeLevel 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<SubjectResponse> listByGradeLevel(ActorContext actor, String gradeLevel) {
        List<SubjectBO> boList =
                subjectRepository.selectByGradeLevel(requireActor(actor).tenantId(), gradeLevel);
        return MapstructUtils.convert(boList, SubjectResponse.class);
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
    public PageData<SubjectResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<SubjectBO> boPage =
                subjectRepository.selectPage(requireActor(actor).tenantId(), pageNum, pageSize);
        List<SubjectResponse> items =
                MapstructUtils.convert(boPage.getItems(), SubjectResponse.class);
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
    public SubjectResponse create(ActorContext actor, SubjectRequest request) {
        actor = requireActor(actor);
        SubjectBO bo = new SubjectBO();
        bo.setCode(request.getCode());
        bo.setName(request.getName());
        bo.setGradeLevel(request.getGradeLevel());
        bo.setIcon(request.getIcon());
        bo.setSortOrder(request.getSortOrder());
        bo.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        subjectRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, SubjectResponse.class);
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, SubjectRequest request) {
        actor = requireActor(actor);
        SubjectBO bo = subjectRepository.selectById(actor.tenantId(), request.getId());
        if (bo != null) {
            bo.setCode(request.getCode());
            bo.setName(request.getName());
            bo.setGradeLevel(request.getGradeLevel());
            bo.setIcon(request.getIcon());
            bo.setSortOrder(request.getSortOrder());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            subjectRepository.update(actor.tenantId(), bo);
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
        subjectRepository.deleteById(requireActor(actor).tenantId(), id);
    }

    private static ActorContext requireActor(ActorContext actor) {
        return java.util.Objects.requireNonNull(actor, "actor is required");
    }
}
