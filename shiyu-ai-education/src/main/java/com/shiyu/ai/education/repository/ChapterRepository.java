package com.shiyu.ai.education.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.education.ChapterDO;
import com.shiyu.ai.dal.mapper.education.ChapterMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChapterRepository {

    @Resource
    private ChapterMapper chapterMapper;

    public ChapterDO selectById(Long id) {
        return chapterMapper.selectOneById(id);
    }

    public List<ChapterDO> selectByTextbookId(Long textbookId) {
        return chapterMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("textbook_id", textbookId)
                        .orderBy("chapter_order"));
    }

    public List<ChapterDO> selectByParentId(Long parentId) {
        return chapterMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("parent_id", parentId)
                        .orderBy("chapter_order"));
    }

    public List<ChapterDO> selectRootChapters(Long textbookId) {
        return chapterMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("textbook_id", textbookId)
                        .isNull("parent_id")
                        .orderBy("chapter_order"));
    }

    public int insert(ChapterDO chapter) {
        return chapterMapper.insert(chapter);
    }

    public int update(ChapterDO chapter) {
        return chapterMapper.update(chapter);
    }

    public int deleteById(Long id) {
        return chapterMapper.deleteById(id);
    }
}
