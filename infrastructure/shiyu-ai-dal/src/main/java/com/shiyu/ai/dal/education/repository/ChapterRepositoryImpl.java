package com.shiyu.ai.dal.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.ChapterBO;
import com.shiyu.ai.dal.education.dataobject.ChapterDO;
import com.shiyu.ai.dal.education.mapper.ChapterMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ChapterRepositoryImpl implements com.shiyu.ai.education.port.repository.ChapterRepository {

    @Resource
    private ChapterMapper chapterMapper;

    public ChapterBO selectById(Long id) {
        return MapstructUtils.convert(chapterMapper.selectOneById(id), ChapterBO.class);
    }

    public List<ChapterBO> selectByTextbookId(Long textbookId) {
        return MapstructUtils.convert(chapterMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("textbook_id", textbookId)
                        .orderBy("chapter_order", true)
        ), ChapterBO.class);
    }

    public List<ChapterBO> selectAll() {
        return MapstructUtils.convert(chapterMapper.selectListByQuery(QueryWrapper.create()), ChapterBO.class);
    }
    public List<ChapterBO> selectRootChapters(Long textbookId) {
        return MapstructUtils.convert(chapterMapper.selectListByQuery(
                QueryWrapper.create().eq("textbook_id", textbookId).eq("parent_id", 0).orderBy("chapter_order", true)), ChapterBO.class);
    }

    public List<ChapterBO> selectByParentId(Long parentId) {
        return MapstructUtils.convert(chapterMapper.selectListByQuery(
                QueryWrapper.create().eq("parent_id", parentId).orderBy("chapter_order", true)), ChapterBO.class);
    }

    public int insert(ChapterBO entity) {
        ChapterDO dataObj = MapstructUtils.convert(entity, ChapterDO.class);
        int rows = chapterMapper.insert(dataObj);
        entity.setId(dataObj.getId());
        return rows;
    }


    public int update(ChapterBO entity) {
        ChapterDO dataObj = MapstructUtils.convert(entity, ChapterDO.class);
        return chapterMapper.update(dataObj);
    }

    public int deleteById(Long id) {
        return chapterMapper.deleteById(id);
    }

}
