package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.application.TextbookService;
import com.shiyu.ai.education.implementation.domain.model.TextbookBO;
import com.shiyu.ai.education.implementation.domain.port.repository.TextbookRepository;
import com.shiyu.ai.education.implementation.web.dto.TextbookResponse;
import com.shiyu.ai.education.implementation.web.request.TextbookRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 提供 教材 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TextbookServiceImpl implements TextbookService {

    /**
     * 查询 教材 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @return 返回 教材 相关操作生成的结果数据。
     */
    @Override
    public PageData<TextbookResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<TextbookBO> boPage =
                textbookRepository.selectPage(requireActor(actor).tenantId(), pageNum, pageSize);
        List<TextbookResponse> items =
                MapstructUtils.convert(boPage.getItems(), TextbookResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    /**
     * textbookRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final TextbookRepository textbookRepository;

    /**
     * 查询 教材 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 教材 相关操作生成的结果数据。
     */
    @Override
    public TextbookResponse getById(ActorContext actor, Long id) {
        TextbookBO bo = textbookRepository.selectById(requireActor(actor).tenantId(), id);
        return MapstructUtils.convert(bo, TextbookResponse.class);
    }

    /**
     * 查询 教材 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param subjectCode 用于完成本次业务处理的 subjectCode 参数。
     * @param grade 用于完成本次业务处理的 grade 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<TextbookResponse> listBySubjectAndGrade(
            ActorContext actor, String subjectCode, Integer grade) {
        List<TextbookBO> boList =
                textbookRepository.selectBySubjectAndGrade(
                        requireActor(actor).tenantId(), subjectCode, grade);
        return MapstructUtils.convert(boList, TextbookResponse.class);
    }

    /**
     * 查询 教材 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<TextbookResponse> listAll(ActorContext actor) {
        List<TextbookBO> boList = textbookRepository.selectAll(requireActor(actor).tenantId());
        return MapstructUtils.convert(boList, TextbookResponse.class);
    }

    /**
     * 执行 教材 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TextbookResponse create(ActorContext actor, TextbookRequest request) {
        actor = requireActor(actor);
        TextbookBO bo = new TextbookBO();
        bo.setName(request.getName());
        bo.setSubjectCode(request.getSubjectCode());
        bo.setGrade(request.getGrade());
        bo.setPublisher(request.getPublisher());
        bo.setIsbn(request.getIsbn());
        bo.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        textbookRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, TextbookResponse.class);
    }

    /**
     * 执行 教材 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, TextbookRequest request) {
        actor = requireActor(actor);
        TextbookBO bo = textbookRepository.selectById(actor.tenantId(), request.getId());
        if (bo != null) {
            bo.setName(request.getName());
            bo.setSubjectCode(request.getSubjectCode());
            bo.setGrade(request.getGrade());
            bo.setPublisher(request.getPublisher());
            bo.setIsbn(request.getIsbn());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            textbookRepository.update(actor.tenantId(), bo);
        }
    }

    /**
     * 执行 教材 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(ActorContext actor, Long id) {
        textbookRepository.deleteById(requireActor(actor).tenantId(), id);
    }

    private static ActorContext requireActor(ActorContext actor) {
        return java.util.Objects.requireNonNull(actor, "actor is required");
    }
}
