package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.ChapterBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface ChapterRepository {
    ChapterBO selectById(TenantId tenantId, Long id);
    List<ChapterBO> selectByTextbookId(TenantId tenantId, Long textbookId);
    List<ChapterBO> selectAll(TenantId tenantId);
    List<ChapterBO> selectRootChapters(TenantId tenantId, Long textbookId);
    List<ChapterBO> selectByParentId(TenantId tenantId, Long parentId);
    int insert(TenantId tenantId, ChapterBO entity);
    int update(TenantId tenantId, ChapterBO entity);
    int deleteById(TenantId tenantId, Long id);
}
