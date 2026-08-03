package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.ResourceBO;
import java.util.List;

public interface ResourceRepository {
    ResourceBO selectById(Long id);
    List<ResourceBO> selectBySubjectCode(String subjectCode);
    List<ResourceBO> selectByType(String type);
    PageData<ResourceBO> selectPage(int pageNum, int pageSize);
    List<ResourceBO> selectAll();
    int insert(ResourceBO entity);
    int update(ResourceBO entity);
    int deleteById(Long id);
}
