package com.shiyu.ai.record.service;

import com.shiyu.ai.dal.bo.record.TagBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Tag 接口
 */

public interface TagService {
    /**
     * Get Page
     * @param Number Number
     * @param Number Number
     * @return 处理结果
     */
    Pair<Long, List<TagBO>> getPage(Number pageNo, Number pageSize, String name);
    /**
     * Get By Id
     * @return 处理结果
     */
    TagBO getById(Long id);
    /**
     * Get By Name
     * @return 处理结果
     */
    TagBO getByName(String name);
    /**
     * Get All
     * @return 处理结果
     */
    List<TagBO> getAll();
    /**
     * Create
     * @param TagBO TagBO
     * @return 处理结果
     */
    TagBO create(TagBO tagBO);
    /**
     * Update
     * @param TagBO TagBO
     * @return 处理结果
     */
    boolean update(TagBO tagBO);
    /**
     * Delete
     * @return 处理结果
     */
    boolean delete(Long id);
}
