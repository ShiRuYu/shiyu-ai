package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.TextbookBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface TextbookRepository {
    TextbookBO selectById(TenantId tenantId, Long id);
    PageData<TextbookBO> selectPage(TenantId tenantId, int pageNum, int pageSize);
    List<TextbookBO> selectBySubjectAndGrade(TenantId tenantId, String subjectCode, Integer grade);
    List<TextbookBO> selectAll(TenantId tenantId);
    int insert(TenantId tenantId, TextbookBO entity);
    int update(TenantId tenantId, TextbookBO entity);
    int deleteById(TenantId tenantId, Long id);
}
