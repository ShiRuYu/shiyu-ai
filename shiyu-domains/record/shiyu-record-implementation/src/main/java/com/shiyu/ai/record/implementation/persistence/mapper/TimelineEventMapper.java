package com.shiyu.ai.record.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;

import com.shiyu.ai.record.implementation.persistence.dataobject.TimelineEventDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 时间轴事件表 数据层
 */
@Mapper
@UseDataSource("agent")
public interface TimelineEventMapper extends BaseMapperFlex<TimelineEventDO> {

}
