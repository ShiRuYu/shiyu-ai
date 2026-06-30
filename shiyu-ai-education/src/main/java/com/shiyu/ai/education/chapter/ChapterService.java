package com.shiyu.ai.education.chapter;

import com.shiyu.ai.dal.dataobject.education.ChapterDO;

import java.util.List;

public interface ChapterService {

    ChapterDO getById(Long id);

    List<ChapterDO> listByTextbookId(Long textbookId);

    List<ChapterDO> listRootChapters(Long textbookId);

    List<ChapterDO> listByParentId(Long parentId);

    ChapterDO create(ChapterDO chapter);

    void update(ChapterDO chapter);

    void deleteById(Long id);
}
