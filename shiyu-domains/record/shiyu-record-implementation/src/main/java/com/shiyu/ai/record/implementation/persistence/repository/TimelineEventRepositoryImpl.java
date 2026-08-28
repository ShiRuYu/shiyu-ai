package com.shiyu.ai.record.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.record.implementation.persistence.dataobject.MediaDO;
import com.shiyu.ai.record.implementation.persistence.dataobject.RecordDO;
import com.shiyu.ai.record.implementation.persistence.dataobject.TimelineEventDO;
import com.shiyu.ai.record.implementation.persistence.mapper.MediaMapper;
import com.shiyu.ai.record.implementation.persistence.mapper.RecordMapper;
import com.shiyu.ai.record.implementation.persistence.mapper.TimelineEventMapper;
import com.shiyu.ai.record.domain.model.TimelineEventBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 时间轴事件数据仓储层
 */
@Component
public class TimelineEventRepositoryImpl implements com.shiyu.ai.record.port.repository.TimelineEventRepository {

    @Resource
    private TimelineEventMapper timelineEventMapper;

    @Resource
    private RecordMapper recordMapper;

    @Resource
    private MediaMapper mediaMapper;

    /**
     * 分页查询时间轴事件列表
     */
    public Pair<Long, List<TimelineEventBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, Long profileId) {
        QueryWrapper countWrapper = QueryWrapper.create()
                .eq(TimelineEventDO::getTenantId, tenantId.value())
                .eq(TimelineEventDO::getProfileId, profileId, profileId != null);
        long total = timelineEventMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(TimelineEventDO::getTenantId, tenantId.value())
                .eq(TimelineEventDO::getProfileId, profileId, profileId != null)
                .orderBy(TimelineEventDO::getEventTime, false);
        
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
    public TimelineEventBO selectByIdWithDetails(TenantId tenantId, Long id) {
        TimelineEventDO eventDO = timelineEventMapper.selectOneByQuery(QueryWrapper.create()
                .eq(TimelineEventDO::getTenantId, tenantId.value())
                .eq(TimelineEventDO::getId, id));
        if (eventDO == null) {
            return null;
        }
        
        TimelineEventBO eventBO = MapstructUtils.convert(eventDO, TimelineEventBO.class);
        
        QueryWrapper recordQuery = QueryWrapper.create()
                .eq(RecordDO::getTenantId, tenantId.value())
                .eq(RecordDO::getEventId, id);
        RecordDO recordDO = recordMapper.selectOneByQuery(recordQuery);
        
        if (recordDO != null) {
            QueryWrapper mediaQuery = QueryWrapper.create()
                    .eq(MediaDO::getTenantId, tenantId.value())
                    .eq(MediaDO::getRecordId, recordDO.getId())
                    .orderBy(MediaDO::getSort, true);
            List<MediaDO> mediaDOs = mediaMapper.selectListByQuery(mediaQuery);
        }
        
        return eventBO;
    }

    /**
     * 创建时间轴事件
     */
    public TimelineEventBO insert(TenantId tenantId, TimelineEventBO eventBO) {
        TimelineEventDO eventDO = MapstructUtils.convert(eventBO, TimelineEventDO.class);
        if (eventDO == null) {
            throw new IllegalArgumentException("TimelineEventBO 转换失败");
        }
        if (eventDO.getEventTime() == null) {
            eventDO.setEventTime(LocalDateTime.now());
        }
        eventDO.setTenantId(tenantId.value());
        timelineEventMapper.insert(eventDO);
        eventBO.setId(eventDO.getId());
        return eventBO;
    }

    /**
     * 更新时间轴事件
     */
    public boolean update(TenantId tenantId, TimelineEventBO eventBO) {
        TimelineEventDO eventDO = MapstructUtils.convert(eventBO, TimelineEventDO.class);
        eventDO.setTenantId(tenantId.value());
        return timelineEventMapper.updateByQuery(eventDO, QueryWrapper.create()
                .eq(TimelineEventDO::getTenantId, tenantId.value())
                .eq(TimelineEventDO::getId, eventBO.getId())) > 0;
    }

    /**
     * 删除时间轴事件
     */
    public boolean deleteById(TenantId tenantId, Long id) {
        return timelineEventMapper.deleteByQuery(QueryWrapper.create()
                .eq(TimelineEventDO::getTenantId, tenantId.value())
                .eq(TimelineEventDO::getId, id)) > 0;
    }

    /**
     * 根据人物ID查询所有事件
     */
    public List<TimelineEventBO> selectByProfileId(TenantId tenantId, Long profileId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(TimelineEventDO::getTenantId, tenantId.value())
                .eq(TimelineEventDO::getProfileId, profileId)
                .orderBy(TimelineEventDO::getEventTime, false);
        
        List<TimelineEventDO> eventDOs = timelineEventMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(eventDOs, TimelineEventBO.class);
    }
}
