package com.shiyu.ai.record.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.util.TenantWorkspaceHelper;
import com.shiyu.ai.dal.dataobject.record.MediaDO;
import com.shiyu.ai.dal.dataobject.record.RecordDO;
import com.shiyu.ai.dal.dataobject.record.TimelineEventDO;
import com.shiyu.ai.dal.mapper.record.MediaMapper;
import com.shiyu.ai.dal.mapper.record.RecordMapper;
import com.shiyu.ai.dal.mapper.record.TimelineEventMapper;
import com.shiyu.ai.record.bo.TimelineEventBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 时间轴事件数据仓储层
 */
@Component
public class TimelineEventRepository {

    @Resource
    private TimelineEventMapper timelineEventMapper;

    @Resource
    private RecordMapper recordMapper;

    @Resource
    private MediaMapper mediaMapper;

    /**
     * 分页查询时间轴事件列表
     */
    public Pair<Long, List<TimelineEventBO>> selectPage(Number pageNo, Number pageSize, Long profileId) {
        QueryWrapper countWrapper = QueryWrapper.create()
                .eq(TimelineEventDO::getProfileId, profileId);
        TenantWorkspaceHelper.applyWorkspaceFilter(countWrapper);
        long total = timelineEventMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(TimelineEventDO::getProfileId, profileId)
                .orderBy(TimelineEventDO::getEventTime, false);
        TenantWorkspaceHelper.applyWorkspaceFilter(queryWrapper);
        
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }
        
        List<TimelineEventDO> eventDOs = timelineEventMapper.selectListByQuery(queryWrapper);
        List<TimelineEventBO> eventBOs = MapstructUtils.convert(eventDOs, TimelineEventBO.class);
        
        return Pair.of(total, eventBOs);
    }

    /**
     * 根据ID查询时间轴事件（包含记录和附件）
     */
    public TimelineEventBO selectByIdWithDetails(Long id) {
        TimelineEventDO eventDO = timelineEventMapper.selectOneById(id);
        if (eventDO == null) {
            return null;
        }
        
        TimelineEventBO eventBO = MapstructUtils.convert(eventDO, TimelineEventBO.class);
        
        QueryWrapper recordQuery = QueryWrapper.create()
                .eq(RecordDO::getEventId, id);
        TenantWorkspaceHelper.applyWorkspaceFilter(recordQuery);
        RecordDO recordDO = recordMapper.selectOneByQuery(recordQuery);
        
        if (recordDO != null) {
            QueryWrapper mediaQuery = QueryWrapper.create()
                    .eq(MediaDO::getRecordId, recordDO.getId())
                    .orderBy(MediaDO::getSort, true);
            TenantWorkspaceHelper.applyWorkspaceFilter(mediaQuery);
            List<MediaDO> mediaDOs = mediaMapper.selectListByQuery(mediaQuery);
        }
        
        return eventBO;
    }

    /**
     * 创建时间轴事件
     */
    public TimelineEventBO insert(TimelineEventBO eventBO) {
        TimelineEventDO eventDO = MapstructUtils.convert(eventBO, TimelineEventDO.class);
        if (eventDO == null) {
            throw new IllegalArgumentException("TimelineEventBO 转换失败");
        }
        if (eventDO.getEventTime() == null) {
            eventDO.setEventTime(LocalDateTime.now());
        }
        timelineEventMapper.insert(eventDO);
        eventBO.setId(eventDO.getId());
        return eventBO;
    }

    /**
     * 更新时间轴事件
     */
    public boolean update(TimelineEventBO eventBO) {
        TimelineEventDO eventDO = MapstructUtils.convert(eventBO, TimelineEventDO.class);
        return timelineEventMapper.update(eventDO) > 0;
    }

    /**
     * 删除时间轴事件
     */
    public boolean deleteById(Long id) {
        return timelineEventMapper.deleteById(id) > 0;
    }

    /**
     * 根据人物ID查询所有事件
     */
    public List<TimelineEventBO> selectByProfileId(Long profileId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(TimelineEventDO::getProfileId, profileId)
                .orderBy(TimelineEventDO::getEventTime, false);
        TenantWorkspaceHelper.applyWorkspaceFilter(queryWrapper);
        
        List<TimelineEventDO> eventDOs = timelineEventMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(eventDOs, TimelineEventBO.class);
    }
}
