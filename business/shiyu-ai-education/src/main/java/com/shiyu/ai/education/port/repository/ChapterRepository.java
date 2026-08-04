package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.education.domain.model.ChapterBO;
import java.util.List;

public interface ChapterRepository {
    ChapterBO selectById(Long id);
    List<ChapterBO> selectByTextbookId(Long textbookId);
    List<ChapterBO> selectAll();
    List<ChapterBO> selectRootChapters(Long textbookId);
    List<ChapterBO> selectByParentId(Long parentId);
    int insert(ChapterBO entity);
    int update(ChapterBO entity);
    int deleteById(Long id);
}
