package com.shiyu.ai.education.resource;

import com.shiyu.ai.dal.dataobject.education.ResourceDO;

import java.util.List;

public interface ResourceService {

    ResourceDO getById(Long id);

    List<ResourceDO> listBySubjectCode(String subjectCode);

    List<ResourceDO> listByType(String type);

    List<ResourceDO> listAll();

    ResourceDO create(ResourceDO resource);

    void update(ResourceDO resource);

    void deleteById(Long id);
}
