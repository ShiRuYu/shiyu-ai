package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.StudentBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface StudentRepository {
    StudentBO selectById(TenantId tenantId, Long id);
    StudentBO selectByUserId(TenantId tenantId, Long userId);
    PageData<StudentBO> selectPage(TenantId tenantId, int pageNum, int pageSize);
    List<StudentBO> selectAll(TenantId tenantId);
    int insert(TenantId tenantId, StudentBO entity);
    int update(TenantId tenantId, StudentBO entity);
    int deleteById(TenantId tenantId, Long id);
}
