package com.shiyu.ai.education.service;

import com.shiyu.ai.dal.dataobject.education.ChapterDO;

import java.util.List;

/**
 * Chapter 接口
 */

public interface ChapterService {

    /**
     * Get By Id
     * @return 处理结果
     */
    ChapterDO getById(Long id);

    /**
     * List By Textbook Id
     * @return 处理结果
     */
    List<ChapterDO> listByTextbookId(Long textbookId);

    /**
     * List Root Chapters
     * @return 处理结果
     */
    List<ChapterDO> listRootChapters(Long textbookId);

    /**
     * List By Parent Id
     * @return 处理结果
     */
    List<ChapterDO> listByParentId(Long parentId);

    /**
     * Create
     * @param ChapterDO ChapterDO
     * @return 处理结果
     */
    ChapterDO create(ChapterDO chapter);

    /**
     * Update
     * @param ChapterDO ChapterDO
     * @return 处理结果
     */
    void update(ChapterDO chapter);

    /**
     * Delete By Id
     * @return 处理结果
     */
    void deleteById(Long id);
}
