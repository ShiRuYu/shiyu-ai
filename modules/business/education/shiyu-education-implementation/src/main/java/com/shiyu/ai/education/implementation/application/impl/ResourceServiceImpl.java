package com.shiyu.ai.education.implementation.application.impl;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
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
 * 提供 资源 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 查询 资源 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 资源 相关操作生成的结果数据。
     */
    @Override
    public ResourceResponse getById(ActorContext actor, Long id) {
        ResourceBO bo = resourceRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, ResourceResponse.class);
    }

    /**
     * 查询 资源 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param subjectCode 用于完成本次业务处理的 subjectCode 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ResourceResponse> listBySubjectCode(ActorContext actor, String subjectCode) {
        List<ResourceBO> boList =
                resourceRepository.selectBySubjectCode(actor.tenantId(), subjectCode);
        return MapstructUtils.convert(boList, ResourceResponse.class);
    }

    /**
     * 查询 资源 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param type 用于完成本次业务处理的 type 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ResourceResponse> listByType(ActorContext actor, String type) {
        List<ResourceBO> boList = resourceRepository.selectByType(actor.tenantId(), type);
        return MapstructUtils.convert(boList, ResourceResponse.class);
    }

    /**
     * 查询 资源 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @return 返回 资源 相关操作生成的结果数据。
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
     * 执行 资源 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
     * 执行 资源 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
     * 查询 资源 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ResourceResponse> listAll(ActorContext actor) {
        List<ResourceBO> boList = resourceRepository.selectAll(actor.tenantId());
        return MapstructUtils.convert(boList, ResourceResponse.class);
    }

    /**
     * 删除或移除 资源 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     */
    public void deleteById(ActorContext actor, Long id) {

        resourceRepository.deleteById(actor.tenantId(), id);
    }
}
