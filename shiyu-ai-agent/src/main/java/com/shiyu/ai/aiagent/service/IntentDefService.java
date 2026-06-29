package com.shiyu.ai.aiagent.service;

import com.shiyu.ai.aiagent.bo.IntentDefBO;
import com.shiyu.ai.model.vo.IdNameOptionVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 意图定义服务层
 */
public interface IntentDefService {

    /**
     * 分页查询意图定义列表
     */
    Pair<Long, List<IntentDefBO>> getPage(Number pageNo, Number pageSize, String agentId, String name, String code, String category);

    /**
     * 根据ID查询意图定义
     */
    IntentDefBO getById(Long id);

    /**
     * 创建意图定义
     */
    IntentDefBO create(IntentDefBO bo);

    /**
     * 更新意图定义
     */
    IntentDefBO update(IntentDefBO bo);

    /**
     * 删除意图定义
     */
    void deleteById(Long id);

    /**
     * 批量删除意图定义
     */
    void deleteByIds(List<Long> ids);

    /**
     * 获取所有意图定义选项（下拉选择用）
     */
    List<IdNameOptionVO> listAllOptions();
}
