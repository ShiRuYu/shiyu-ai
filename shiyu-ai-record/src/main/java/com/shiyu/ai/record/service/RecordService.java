package com.shiyu.ai.record.service;

import com.shiyu.ai.dal.bo.record.RecordBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Record 接口
 */

public interface RecordService {
    /**
     * Get Page
     * @param Number Number
     * @param Number Number
     * @return 处理结果
     */
    Pair<Long, List<RecordBO>> getPage(Number pageNo, Number pageSize, Long eventId);
    /**
     * Get By Id
     * @return 处理结果
     */
    RecordBO getById(Long id);
    /**
     * Create
     * @param RecordBO RecordBO
     * @return 处理结果
     */
    RecordBO create(RecordBO recordBO);
    /**
     * Update
     * @param RecordBO RecordBO
     * @return 处理结果
     */
    boolean update(RecordBO recordBO);
    /**
     * Delete
     * @return 处理结果
     */
    boolean delete(Long id);
}
