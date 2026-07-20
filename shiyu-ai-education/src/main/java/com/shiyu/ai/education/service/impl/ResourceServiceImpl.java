package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.education.bo.ResourceBO;
import com.shiyu.ai.dal.education.repository.ResourceRepository;
import com.shiyu.ai.education.dto.ResourceResponse;
import com.shiyu.ai.education.request.ResourceRequest;
import com.shiyu.ai.education.service.ResourceService;
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
    public ResourceResponse getById(Long id) {
        ResourceBO bo = resourceRepository.selectById(id);
        return MapstructUtils.convert(bo, ResourceResponse.class);
    }

    @Override
    public List<ResourceResponse> listBySubjectCode(String subjectCode) {
        List<ResourceBO> boList = resourceRepository.selectBySubjectCode(subjectCode);
        return MapstructUtils.convert(boList, ResourceResponse.class);
    }

    @Override
    public List<ResourceResponse> listByType(String type) {
        List<ResourceBO> boList = resourceRepository.selectByType(type);
        return MapstructUtils.convert(boList, ResourceResponse.class);
    }

    @Override
    public PageData<ResourceResponse> page(int pageNum, int pageSize) {
        PageData<ResourceBO> boPage = resourceRepository.selectPage(pageNum, pageSize);
        List<ResourceResponse> items = MapstructUtils.convert(boPage.getItems(), ResourceResponse.class);
        return new PageData<>(items, boPage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceResponse create(ResourceRequest request) {
        ResourceBO bo = new ResourceBO();
        bo.setName(request.getName());
        bo.setType(request.getType());
        bo.setUrl(request.getUrl());
        bo.setDescription(request.getDescription());
        resourceRepository.insert(bo);
        return MapstructUtils.convert(bo, ResourceResponse.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ResourceRequest request) {
        ResourceBO bo = resourceRepository.selectById(request.getId());
        if (bo != null) {
            bo.setName(request.getName());
            bo.setType(request.getType());
            bo.setUrl(request.getUrl());
            bo.setDescription(request.getDescription());
            resourceRepository.update(bo);
        }
    }

    @Override
    public List<ResourceResponse> listAll() {
        List<ResourceBO> boList = resourceRepository.selectAll();
        return MapstructUtils.convert(boList, ResourceResponse.class);
    }

    public void deleteById(Long id) {

        resourceRepository.deleteById(id);
    }
}
