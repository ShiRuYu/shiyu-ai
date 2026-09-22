package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
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
 * 提供 学科 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 查询 学科 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 学科 相关操作生成的结果数据。
     */
    @Override
    public SubjectResponse getById(ActorContext actor, Long id) {
        SubjectBO bo = subjectRepository.selectById(requireActor(actor).tenantId(), id);
        return MapstructUtils.convert(bo, SubjectResponse.class);
    }

    /**
     * 查询 学科 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 学科 相关操作生成的结果数据。
     */
    @Override
    public SubjectResponse getByCode(ActorContext actor, String code) {
        SubjectBO bo = subjectRepository.selectByCode(requireActor(actor).tenantId(), code);
        return MapstructUtils.convert(bo, SubjectResponse.class);
    }

    /**
     * 查询 学科 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param gradeLevel 用于完成本次业务处理的 gradeLevel 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<SubjectResponse> listByGradeLevel(ActorContext actor, String gradeLevel) {
        List<SubjectBO> boList =
                subjectRepository.selectByGradeLevel(requireActor(actor).tenantId(), gradeLevel);
        return MapstructUtils.convert(boList, SubjectResponse.class);
    }

    /**
     * 查询 学科 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @return 返回 学科 相关操作生成的结果数据。
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
     * 执行 学科 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
     * 执行 学科 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
     * 执行 学科 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
