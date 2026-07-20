package com.shiyu.ai.record.service;

import com.shiyu.ai.dal.record.bo.MediaBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Media 接口
 */

public interface MediaService {
    /**
     * Get Page
     * @param Number Number
     * @param Number Number
     * @return 处理结果
     */
    Pair<Long, List<MediaBO>> getPage(Number pageNo, Number pageSize, Long recordId);
    /**
     * Get By Id
     * @return 处理结果
     */
    MediaBO getById(Long id);
    /**
     * Create
     * @param MediaBO MediaBO
     * @return 处理结果
     */
    MediaBO create(MediaBO mediaBO);
    /**
     * Update
     * @param MediaBO MediaBO
     * @return 处理结果
     */
    boolean update(MediaBO mediaBO);
    /**
     * Delete
     * @return 处理结果
     */
    boolean delete(Long id);
}
