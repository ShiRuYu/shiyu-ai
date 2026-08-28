package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.ResourceBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface ResourceRepository {
    ResourceBO selectById(TenantId tenantId, Long id);
    List<ResourceBO> selectBySubjectCode(TenantId tenantId, String subjectCode);
    List<ResourceBO> selectByType(TenantId tenantId, String type);
    PageData<ResourceBO> selectPage(TenantId tenantId, int pageNum, int pageSize);
    List<ResourceBO> selectAll(TenantId tenantId);
    int insert(TenantId tenantId, ResourceBO entity);
    int update(TenantId tenantId, ResourceBO entity);
    int deleteById(TenantId tenantId, Long id);
}
