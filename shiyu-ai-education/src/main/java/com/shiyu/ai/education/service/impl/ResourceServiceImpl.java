package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.dal.dataobject.education.ResourceDO;
import com.shiyu.ai.dal.repository.education.ResourceRepository;
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
    public ResourceDO getById(Long id) {
        return resourceRepository.selectById(id);
    }

    @Override
    public List<ResourceDO> listBySubjectCode(String subjectCode) {
        return resourceRepository.selectBySubjectCode(subjectCode);
    }

    @Override
    public List<ResourceDO> listByType(String type) {
        return resourceRepository.selectByType(type);
    }

    @Override
    public PageData<ResourceDO> page(int pageNum, int pageSize) {
        return resourceRepository.selectPage(pageNum, pageSize);
    }

    public List<ResourceDO> listAll() {
        return resourceRepository.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceDO create(ResourceDO resource) {
        resourceRepository.insert(resource);
        return resource;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ResourceDO resource) {
        resourceRepository.update(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        resourceRepository.deleteById(id);
    }
}
