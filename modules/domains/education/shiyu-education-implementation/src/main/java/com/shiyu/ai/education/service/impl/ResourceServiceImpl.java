package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.ResourceBO;
import com.shiyu.ai.education.port.repository.ResourceRepository;
import com.shiyu.ai.education.dto.ResourceResponse;
import com.shiyu.ai.education.request.ResourceRequest;
import com.shiyu.ai.education.service.ResourceService;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;

    @Override
    public ResourceResponse getById(ActorContext actor, Long id) {
        ResourceBO bo = resourceRepository.selectById(actor.tenantId(), id);
        return MapstructUtils.convert(bo, ResourceResponse.class);
    }

    @Override
    public List<ResourceResponse> listBySubjectCode(ActorContext actor, String subjectCode) {
        List<ResourceBO> boList = resourceRepository.selectBySubjectCode(actor.tenantId(), subjectCode);
        return MapstructUtils.convert(boList, ResourceResponse.class);
    }

    @Override
    public List<ResourceResponse> listByType(ActorContext actor, String type) {
        List<ResourceBO> boList = resourceRepository.selectByType(actor.tenantId(), type);
        return MapstructUtils.convert(boList, ResourceResponse.class);
    }

    @Override
    public PageData<ResourceResponse> page(ActorContext actor, int pageNum, int pageSize) {
        PageData<ResourceBO> boPage = resourceRepository.selectPage(actor.tenantId(), pageNum, pageSize);
        List<ResourceResponse> items = MapstructUtils.convert(boPage.getItems(), ResourceResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

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

    @Override
    public List<ResourceResponse> listAll(ActorContext actor) {
        List<ResourceBO> boList = resourceRepository.selectAll(actor.tenantId());
        return MapstructUtils.convert(boList, ResourceResponse.class);
    }

    public void deleteById(ActorContext actor, Long id) {

        resourceRepository.deleteById(actor.tenantId(), id);
    }
}
