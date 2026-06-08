package com.shiyu.ai.agent.biz.record.service;

import com.shiyu.ai.agent.domain.bo.TimelineEventBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 时间轴事件服务接口
 */
public interface TimelineEventService {

    /**
     * 分页查询时间轴事件列表
     */
    Pair<Long, List<TimelineEventBO>> getPage(Number pageNo, Number pageSize, Long profileId);

    /**
     * 根据ID查询时间轴事件
     */
    TimelineEventBO getById(Long id);

    /**
     * 创建时间轴事件
     */
    TimelineEventBO create(TimelineEventBO eventBO);

    /**
     * 更新时间轴事件
     */
    boolean update(TimelineEventBO eventBO);

    /**
     * 删除时间轴事件
     */
    boolean delete(Long id);

    /**
     * 查询人物的时间轴
     */
    List<TimelineEventBO> getTimelineByProfileId(Long profileId);
}
