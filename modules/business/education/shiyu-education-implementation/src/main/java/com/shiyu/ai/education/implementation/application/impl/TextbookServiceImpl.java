package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
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
 * {@code TextbookServiceImpl} 实现教育模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TextbookServiceImpl implements TextbookService {

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
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public TextbookResponse getById(ActorContext actor, Long id) {
        TextbookBO bo = textbookRepository.selectById(requireActor(actor).tenantId(), id);
        return MapstructUtils.convert(bo, TextbookResponse.class);
    }

    /**
     * {@code listBySubjectAndGrade} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param subjectCode 参数值，用于执行当前操作。
     * @param grade 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code listAll} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<TextbookResponse> listAll(ActorContext actor) {
        List<TextbookBO> boList = textbookRepository.selectAll(requireActor(actor).tenantId());
        return MapstructUtils.convert(boList, TextbookResponse.class);
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
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
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
     * {@code deleteById} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
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
