package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.application.ResourceService;
import com.shiyu.ai.education.implementation.domain.model.ResourceBO;
import com.shiyu.ai.education.implementation.domain.port.repository.ResourceRepository;
import com.shiyu.ai.education.implementation.web.dto.ResourceResponse;
import com.shiyu.ai.education.implementation.web.request.ResourceRequest;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@code ResourceServiceImpl} 实现教育模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    /**
     * 资源仓储，表示当前对象中的对应属性。
     */
    private final ResourceRepository resourceRepository;

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public ResourceResponse getById(ActorContext actor, Long id) {
        ResourceBO bo = resourceRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, ResourceResponse.class);
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
    public List<ResourceResponse> listBySubjectCode(ActorContext actor, String subjectCode) {
        List<ResourceBO> boList =
                resourceRepository.selectBySubjectCode(actor.tenantId(), subjectCode);
        return MapstructUtils.convert(boList, ResourceResponse.class);
    }

    /**
     * {@code listByType} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param type 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<ResourceResponse> listByType(ActorContext actor, String type) {
        List<ResourceBO> boList = resourceRepository.selectByType(actor.tenantId(), type);
        return MapstructUtils.convert(boList, ResourceResponse.class);
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
    public PageData<ResourceResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<ResourceBO> boPage =
                resourceRepository.selectPage(actor.tenantId(), pageNum, pageSize);
        List<ResourceResponse> items =
                MapstructUtils.convert(boPage.getItems(), ResourceResponse.class);
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
    public ResourceResponse create(ActorContext actor, ResourceRequest request) {
        ResourceBO bo = new ResourceBO();
        bo.setName(request.getName());
        bo.setType(request.getType());
        bo.setSubjectCode(request.getSubjectCode());
        bo.setGrade(request.getGrade());
        bo.setDifficulty(request.getDifficulty());
        bo.setCoverUrl(request.getCoverUrl());
        bo.setUrl(request.getUrl());
        bo.setDescription(request.getDescription());
        bo.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        resourceRepository.insert(actor.tenantId(), bo);
        return MapstructUtils.convert(bo, ResourceResponse.class);
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActorContext actor, ResourceRequest request) {
        ResourceBO bo = resourceRepository.selectById(actor.tenantId(), request.getId());
        if (bo != null) {
            bo.setName(request.getName());
            bo.setType(request.getType());
            bo.setSubjectCode(request.getSubjectCode());
            bo.setGrade(request.getGrade());
            bo.setDifficulty(request.getDifficulty());
            bo.setCoverUrl(request.getCoverUrl());
            bo.setUrl(request.getUrl());
            bo.setDescription(request.getDescription());
            if (request.getStatus() != null) bo.setStatus(request.getStatus());
            resourceRepository.update(actor.tenantId(), bo);
        }
    }

    /**
     * {@code listAll} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<ResourceResponse> listAll(ActorContext actor) {
        List<ResourceBO> boList = resourceRepository.selectAll(actor.tenantId());
        return MapstructUtils.convert(boList, ResourceResponse.class);
    }

    /**
     * {@code deleteById} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    public void deleteById(ActorContext actor, Long id) {

        resourceRepository.deleteById(actor.tenantId(), id);
    }
}
