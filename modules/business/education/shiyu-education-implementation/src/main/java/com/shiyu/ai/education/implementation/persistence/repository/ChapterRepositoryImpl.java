package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.ChapterBO;
import com.shiyu.ai.education.implementation.persistence.dataobject.ChapterDO;
import com.shiyu.ai.education.implementation.persistence.mapper.ChapterMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@code ChapterRepositoryImpl} 实现教育模块的持久化端口，负责在领域对象与存储模型之间转换。
 */
@Component
public class ChapterRepositoryImpl
        implements com.shiyu.ai.education.implementation.domain.port.repository.ChapterRepository {

    /**
     * 章节映射器，表示当前对象中的对应属性。
     */
    @Resource private ChapterMapper chapterMapper;

    public ChapterBO selectById(TenantId tenantId, Long id) {
        return MapstructUtils.convert(
                chapterMapper.selectOneByQuery(
                        QueryWrapper.create()
                                .eq(ChapterDO::getTenantId, tenantId.value())
                                .eq(ChapterDO::getId, id)),
                ChapterBO.class);
    }

    public List<ChapterBO> selectByTextbookId(TenantId tenantId, Long textbookId) {
        return MapstructUtils.convert(
                chapterMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq(ChapterDO::getTenantId, tenantId.value())
                                .eq("textbook_id", textbookId)
                                .orderBy("chapter_order", true)),
                ChapterBO.class);
    }

    public List<ChapterBO> selectAll(TenantId tenantId) {
        return MapstructUtils.convert(
                chapterMapper.selectListByQuery(
                        QueryWrapper.create().eq(ChapterDO::getTenantId, tenantId.value())),
                ChapterBO.class);
    }

    public List<ChapterBO> selectRootChapters(TenantId tenantId, Long textbookId) {
        return MapstructUtils.convert(
                chapterMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq(ChapterDO::getTenantId, tenantId.value())
                                .eq("textbook_id", textbookId)
                                .eq("parent_id", 0)
                                .orderBy("chapter_order", true)),
                ChapterBO.class);
    }

    public List<ChapterBO> selectByParentId(TenantId tenantId, Long parentId) {
        return MapstructUtils.convert(
                chapterMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq(ChapterDO::getTenantId, tenantId.value())
                                .eq("parent_id", parentId)
                                .orderBy("chapter_order", true)),
                ChapterBO.class);
    }

    public int insert(TenantId tenantId, ChapterBO entity) {
        ChapterDO dataObj = MapstructUtils.convert(entity, ChapterDO.class);
        dataObj.setTenantId(tenantId.value());
        int rows = EducationWriteGuard.require(chapterMapper.insert(dataObj), "insert chapter");
        entity.setId(dataObj.getId());
        return rows;
    }

    public int update(TenantId tenantId, ChapterBO entity) {
        ChapterDO dataObj = MapstructUtils.convert(entity, ChapterDO.class);
        dataObj.setTenantId(tenantId.value());
        return EducationWriteGuard.require(
                chapterMapper.updateByQuery(
                        dataObj,
                        QueryWrapper.create()
                                .eq(ChapterDO::getTenantId, tenantId.value())
                                .eq(ChapterDO::getId, entity.getId())),
                "update chapter");
    }

    public int deleteById(TenantId tenantId, Long id) {
        return EducationWriteGuard.require(
                chapterMapper.deleteByQuery(
                        QueryWrapper.create()
                                .eq(ChapterDO::getTenantId, tenantId.value())
                                .eq(ChapterDO::getId, id)),
                "delete chapter");
    }
}
